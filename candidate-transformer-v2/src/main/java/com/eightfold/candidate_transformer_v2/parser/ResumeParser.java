package com.eightfold.candidate_transformer_v2.parser;

import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.model.Education;
import com.eightfold.candidate_transformer_v2.model.Experience;
import com.eightfold.candidate_transformer_v2.model.Project;
import com.eightfold.candidate_transformer_v2.model.Skill;

/**
 * Section-based resume parser.
 *
 * SKILLS EXTRACTION RULE (strict):
 *   - Locate the Skills / Technical Skills section heading.
 *   - Collect lines ONLY until the next section heading is detected.
 *   - Match each token against the whitelist ONLY.
 *   - NEVER scan any other section (projects, certifications, experience, etc.).
 *   - NEVER infer technologies from certificate names, project names, or descriptions.
 */
public class ResumeParser {

    // ===================================================================
    // Patterns
    // ===================================================================
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("\\+?\\d[\\d\\s().\\-]{7,}\\d");

    private static final Pattern YEAR_PATTERN =
            Pattern.compile("\\b(20\\d{2}|19\\d{2})\\b");

    private static final Pattern YEAR_RANGE_PATTERN =
            Pattern.compile(
                    "\\b((?:20|19)\\d{2})\\s*[-\u2013\u2014to]+\\s*" +
                    "((?:20|19)\\d{2}|[Pp]resent|[Cc]urrent|[Nn]ow)\\b");

    // ===================================================================
    // Section heading sets
    // ===================================================================

    /** Headings that mark the START of a Skills section. */
    private static final Set<String> SKILL_HEADINGS = new HashSet<>(Arrays.asList(
            "skills",
            "technical skills",
            "technical expertise",
            "core skills",
            "programming skills",
            "technology stack",
            "tech stack",
            "professional skills",
            "technical competencies",
            "core competencies",
            "technologies",
            "tools and technologies",
            "tools & technologies",
            "skill set",
            "key skills"
    ));

    /**
     * Headings that mark the END of a Skills section.
     * Once any of these headings is encountered after the Skills section starts,
     * skill parsing stops immediately.
     */
    private static final Set<String> STOP_HEADINGS = new HashSet<>(Arrays.asList(
            "education", "academic background", "educational qualification",
            "educational qualifications", "academic qualifications", "qualification",
            "experience", "work experience", "professional experience",
            "employment history", "work history",
            "internship", "internships", "industrial training",
            "projects", "personal projects", "academic projects",
            "key projects", "project experience",
            "certifications", "certification", "certificates",
            "courses", "training", "online courses",
            "achievements", "awards", "honors", "accomplishments",
            "languages", "hobbies", "interests",
            "summary", "professional summary", "career objective",
            "objective", "profile", "about me", "about",
            "research", "publications", "references",
            "volunteer experience", "extra curricular activities",
            "extracurricular", "activities"
    ));

    /** All known headings combined — used for section segmentation. */
    private static final Set<String> ALL_HEADINGS = new HashSet<>();
    static {
        ALL_HEADINGS.addAll(SKILL_HEADINGS);
        ALL_HEADINGS.addAll(STOP_HEADINGS);
    }

    // ===================================================================
    // Technology whitelist  (key = alias in lower-case, value = canonical)
    // ===================================================================
    private static final Map<String, String> TECH_WHITELIST = buildWhitelist();

    // ===================================================================
    // Public entry point
    // ===================================================================

    public Candidate parseCandidate(MultipartFile resumeFile) throws IOException {
        String rawText = extractPdfText(resumeFile);
        String[] lines = rawText.split("\\r?\\n");

        String fullName  = extractFullName(lines);
        String email     = extractEmail(rawText);
        String phone     = extractPhone(rawText);

        // Section-aware parsing
        Map<String, List<String>> sections = segmentSections(lines);

        List<Skill>      skills         = extractSkillsFromSkillSectionOnly(lines);
        List<Experience> experience     = extractExperience(sections);
        List<Education>  education      = extractEducation(sections);
        List<Project>    projects       = extractProjects(sections);
        List<String>     certifications = extractCertifications(sections);
        int              yearsExp       = calculateYearsExperience(experience);

        return Candidate.builder()
                .fullName(fullName)
                .emails(email != null ? List.of(email) : List.of())
                .phones(phone != null ? List.of(phone) : List.of())
                .skills(skills)
                .experience(experience)
                .education(education)
                .projects(projects)
                .certifications(certifications)
                .yearsExperience(yearsExp)
                .overallConfidence(0.90)
                .build();
    }

    // ===================================================================
    // PDF text extraction
    // ===================================================================

    private String extractPdfText(MultipartFile file) throws IOException {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    // ===================================================================
    // Section segmentation (generic — used by experience, education, etc.)
    // ===================================================================

    private Map<String, List<String>> segmentSections(String[] lines) {
        Map<String, List<String>> sections = new LinkedHashMap<>();
        String currentSection = "header";
        sections.put(currentSection, new ArrayList<>());

        for (String line : lines) {
            String trimmed = line.trim();
            String normalized = normalizeHeading(trimmed);
            if (isKnownHeading(normalized)) {
                currentSection = normalized;
                sections.putIfAbsent(currentSection, new ArrayList<>());
            } else {
                sections.get(currentSection).add(trimmed);
            }
        }
        return sections;
    }

    /** Normalises a raw line to a heading key: lower-case, letters and spaces only. */
    private String normalizeHeading(String line) {
        return line.toLowerCase()
                   .replaceAll("[^a-z\\s]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    private boolean isKnownHeading(String normalized) {
        if (normalized.isEmpty() || normalized.length() > 60) return false;
        return ALL_HEADINGS.contains(normalized);
    }

    /** Returns all lines belonging to sections whose heading is in the given set. */
    private List<String> getLinesForSection(Map<String, List<String>> sections,
                                            Set<String> headings) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : sections.entrySet()) {
            if (headings.contains(entry.getKey())) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    // ===================================================================
    // SKILLS — strict section-only extraction (THE KEY METHOD)
    // ===================================================================

    /**
     * Extracts technical skills by scanning ONLY the Skills section.
     *
     * Algorithm:
     *  1. Walk the resume line-by-line.
     *  2. When a SKILL_HEADING is found, enter "inside skills" mode.
     *  3. While inside, split each line on common delimiters and match
     *     each token against TECH_WHITELIST using exact / cleaned lookup.
     *  4. When a STOP_HEADING is found, exit immediately — no more scanning.
     *  5. If no skill heading is found, return an empty list.
     *
     * Guarantees:
     *  - Only tokens present in TECH_WHITELIST are ever added.
     *  - Certificate names, project descriptions, internship names,
     *    sentences, dates, and company names are all silently discarded.
     *  - Result is sorted alphabetically and de-duplicated.
     */
    private List<Skill> extractSkillsFromSkillSectionOnly(String[] lines) {
        // Use TreeSet for automatic alphabetical deduplication
        Set<String> found = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        boolean insideSkills = false;

        for (String rawLine : lines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) continue;

            String normalized = normalizeHeading(trimmed);

            // Check if this line is a section heading
            if (isKnownHeading(normalized)) {
                if (SKILL_HEADINGS.contains(normalized)) {
                    insideSkills = true;   // entered a skills section
                } else if (STOP_HEADINGS.contains(normalized)) {
                    if (insideSkills) {
                        break;             // skills section ended — stop scanning
                    }
                }
                continue; // heading lines are never content
            }

            if (!insideSkills) continue;

            // Split line on all common delimiters
            String[] tokens = trimmed.split("[,;|/\\\\•·\\u2022\\u2023\\u25E6\\u2043\\t]+");
            for (String token : tokens) {
                String canonical = matchWhitelistToken(token.trim());
                if (canonical != null) {
                    found.add(canonical);
                }
            }
        }

        List<Skill> skills = new ArrayList<>();
        for (String name : found) {
            skills.add(Skill.builder()
                    .name(name)
                    .confidence(0.90)
                    .source("Resume - Skills Section")
                    .build());
        }
        return skills;
    }

    /**
     * Matches a single whitespace-trimmed token against the technology whitelist.
     * Only returns a value when the token exactly matches a known alias.
     * Rejects multi-word tokens that look like sentences (> 5 words).
     */
    private String matchWhitelistToken(String token) {
        if (token == null || token.isBlank()) return null;
        if (token.split("\\s+").length > 5)  return null; // reject sentences

        String lower = token.toLowerCase().trim();

        // 1. Direct lookup
        if (TECH_WHITELIST.containsKey(lower)) return TECH_WHITELIST.get(lower);

        // 2. Strip parenthetical qualifier: "Python (3.x)" → "python"
        String noParens = lower.replaceAll("\\s*\\(.*?\\)\\s*", "").trim();
        if (!noParens.equals(lower) && TECH_WHITELIST.containsKey(noParens))
            return TECH_WHITELIST.get(noParens);

        // 3. Strip trailing version: "Node.js v18" → "node.js"
        String noVersion = lower.replaceAll("\\s+v?\\d[\\d.]*$", "").trim();
        if (!noVersion.equals(lower) && TECH_WHITELIST.containsKey(noVersion))
            return TECH_WHITELIST.get(noVersion);

        return null; // not a known technology
    }

    // ===================================================================
    // Name / contact
    // ===================================================================

    private String extractFullName(String[] lines) {
        for (String raw : lines) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;
            if (EMAIL_PATTERN.matcher(trimmed).find()) continue;
            if (PHONE_PATTERN.matcher(trimmed).find()) continue;
            if (isKnownHeading(normalizeHeading(trimmed))) continue;
            if (trimmed.matches("(?i).*\\b(resume|curriculum vitae|cv)\\b.*")) continue;
            // Accept 2–5 words each starting with a capital letter
            if (trimmed.matches("[A-Z][a-zA-Z.'\\-]+(\\s+[A-Z][a-zA-Z.'\\-]+){1,4}")) {
                return trimmed;
            }
        }
        // Fallback: first non-empty non-heading line
        for (String raw : lines) {
            String trimmed = raw.trim();
            if (!trimmed.isEmpty() && !isKnownHeading(normalizeHeading(trimmed))) {
                return trimmed;
            }
        }
        return "";
    }

    private String extractEmail(String text) {
        Matcher m = EMAIL_PATTERN.matcher(text);
        return m.find() ? m.group().trim() : null;
    }

    private String extractPhone(String text) {
        Matcher m = PHONE_PATTERN.matcher(text);
        return m.find() ? m.group().trim() : null;
    }

    // ===================================================================
    // Experience
    // ===================================================================

    private static final Set<String> EXPERIENCE_HEADINGS = new HashSet<>(Arrays.asList(
            "experience", "work experience", "professional experience",
            "employment history", "work history",
            "internship", "internships", "industrial training"
    ));

    private List<Experience> extractExperience(Map<String, List<String>> sections) {
        List<String> lines = getLinesForSection(sections, EXPERIENCE_HEADINGS);
        List<Experience> list = new ArrayList<>();
        if (lines.isEmpty()) return list;

        Experience current = null;
        StringBuilder summary = new StringBuilder();

        for (String line : lines) {
            if (line.isBlank()) {
                if (current != null && summary.length() > 0) {
                    current.setSummary(summary.toString().trim());
                    summary.setLength(0);
                }
                continue;
            }
            Matcher range = YEAR_RANGE_PATTERN.matcher(line);
            if (range.find()) {
                if (current != null) {
                    if (summary.length() > 0) { current.setSummary(summary.toString().trim()); summary.setLength(0); }
                    list.add(current);
                }
                current = Experience.builder()
                        .startDate(range.group(1)).endDate(range.group(2)).build();
                String before = line.substring(0, range.start()).trim();
                if (!before.isEmpty()) current.setTitle(cleanTitle(before));
            } else if (current == null) {
                current = Experience.builder().title(cleanTitle(line)).build();
            } else if (current.getTitle() == null || current.getTitle().isBlank()) {
                current.setTitle(cleanTitle(line));
            } else if ((current.getCompany() == null || current.getCompany().isBlank())
                       && looksLikeCompanyName(line)) {
                current.setCompany(line.trim());
            } else {
                summary.append(line).append(" ");
            }
        }
        if (current != null) {
            if (summary.length() > 0) current.setSummary(summary.toString().trim());
            list.add(current);
        }
        return list;
    }

    private String cleanTitle(String raw) {
        return raw.replaceAll(
                "(?i)\\b(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec"
                + "|january|february|march|april|june|july|august"
                + "|september|october|november|december)\\b.*", "")
                .replaceAll("[\\-\u2013\u2014|].*$", "").trim();
    }

    private boolean looksLikeCompanyName(String line) {
        return line.matches(".*(?i)(pvt|ltd|llc|inc|technologies|solutions|systems"
                + "|consulting|services|institute|college|university|labs|corp).*");
    }

    // ===================================================================
    // Education
    // ===================================================================

    private static final Set<String> EDUCATION_HEADINGS = new HashSet<>(Arrays.asList(
            "education", "academic background", "educational qualification",
            "educational qualifications", "academic qualifications", "qualification"
    ));

    private List<Education> extractEducation(Map<String, List<String>> sections) {
        List<String> lines = getLinesForSection(sections, EDUCATION_HEADINGS);
        List<Education> list = new ArrayList<>();
        if (lines.isEmpty()) return list;

        Education current = null;
        for (String line : lines) {
            if (line.isBlank()) {
                if (current != null && isEduComplete(current)) { list.add(current); current = null; }
                continue;
            }
            if (hasDegreeKeyword(line)) {
                if (current != null && isEduComplete(current)) list.add(current);
                current = Education.builder()
                        .degree(extractDegree(line))
                        .field(extractField(line))
                        .endYear(extractYear(line))
                        .build();
            } else if (current != null) {
                if ((current.getInstitution() == null || current.getInstitution().isBlank())
                        && looksLikeInstitution(line)) {
                    current.setInstitution(line.trim());
                }
                if (current.getEndYear() == null) {
                    Integer yr = extractYear(line);
                    if (yr != null) current.setEndYear(yr);
                }
            } else {
                current = Education.builder().institution(line.trim()).build();
            }
        }
        if (current != null && isEduComplete(current)) list.add(current);
        return list;
    }

    private boolean hasDegreeKeyword(String line) {
        return line.matches("(?i).*\\b(b\\.?tech|b\\.?e\\.?|m\\.?tech|m\\.?e\\.?"
                + "|b\\.?sc|m\\.?sc|b\\.?com|m\\.?com|b\\.?a\\.?|m\\.?a\\.?"
                + "|phd|ph\\.?d|bca|mca|diploma|bachelor|master|doctorate"
                + "|10th|12th|ssc|hsc|intermediate|secondary)\\b.*");
    }

    private String extractDegree(String line) {
        return line.replaceAll("\\s*[-\u2013\u2014|].*$", "")
                   .replaceAll("(?i)\\s+(from|at|in|,)\\s+.*$", "").trim();
    }

    private String extractField(String line) {
        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(line);
        return m.find() ? m.group(1).trim() : null;
    }

    private Integer extractYear(String line) {
        Matcher m = YEAR_PATTERN.matcher(line);
        return m.find() ? Integer.parseInt(m.group(1)) : null;
    }

    private boolean looksLikeInstitution(String line) {
        return line.matches(".*(?i)(institute|college|university|school|academy"
                + "|vidyalaya|polytechnic|iit|nit|bits|vit|srm|jntu|anna).*");
    }

    private boolean isEduComplete(Education edu) {
        return edu.getDegree() != null || edu.getInstitution() != null;
    }

    // ===================================================================
    // Projects
    // ===================================================================

    private static final Set<String> PROJECT_HEADINGS = new HashSet<>(Arrays.asList(
            "projects", "personal projects", "academic projects",
            "key projects", "project experience"
    ));

    private List<Project> extractProjects(Map<String, List<String>> sections) {
        List<String> lines = getLinesForSection(sections, PROJECT_HEADINGS);
        List<Project> list = new ArrayList<>();
        if (lines.isEmpty()) return list;

        Project current = null;
        StringBuilder desc = new StringBuilder();

        for (String line : lines) {
            if (line.isBlank()) {
                if (current != null) {
                    if (desc.length() > 0) current.setDescription(desc.toString().trim());
                    list.add(current); current = null; desc.setLength(0);
                }
                continue;
            }
            // A line with no leading punctuation and not too long → project name
            if (current == null && !line.startsWith("•") && !line.startsWith("-")
                    && line.length() < 80) {
                current = Project.builder().projectName(line.trim()).build();
            } else if (current != null) {
                desc.append(line).append(" ");
            }
        }
        if (current != null) {
            if (desc.length() > 0) current.setDescription(desc.toString().trim());
            list.add(current);
        }
        return list;
    }

    // ===================================================================
    // Certifications
    // ===================================================================

    private static final Set<String> CERT_HEADINGS = new HashSet<>(Arrays.asList(
            "certifications", "certification", "certificates",
            "courses", "training", "online courses"
    ));

    private List<String> extractCertifications(Map<String, List<String>> sections) {
        List<String> lines = getLinesForSection(sections, CERT_HEADINGS);
        List<String> certs = new ArrayList<>();
        for (String line : lines) {
            String t = line.trim().replaceAll("^[•\\-–—*\\u2022]+\\s*", "").trim();
            if (!t.isBlank() && t.length() > 3) certs.add(t);
        }
        return certs;
    }

    // ===================================================================
    // Years of experience — from Experience section dates ONLY
    // ===================================================================

    private int calculateYearsExperience(List<Experience> experiences) {
        if (experiences.isEmpty()) return 0;
        int currentYear = Year.now().getValue();
        int earliest = currentYear;
        boolean found = false;
        for (Experience exp : experiences) {
            String sd = exp.getStartDate();
            if (sd != null && sd.matches(".*\\d{4}.*")) {
                Matcher m = YEAR_PATTERN.matcher(sd);
                if (m.find()) {
                    int yr = Integer.parseInt(m.group(1));
                    if (yr >= 1990 && yr < earliest) { earliest = yr; found = true; }
                }
            }
        }
        return found ? Math.max(0, currentYear - earliest) : 0;
    }

    // ===================================================================
    // Technology whitelist
    // ===================================================================

    private static Map<String, String> buildWhitelist() {
        Map<String, String> m = new LinkedHashMap<>();

        // --- Programming Languages ---
        m.put("java",           "Java");
        m.put("python",         "Python");
        m.put("c++",            "C++");
        m.put("c#",             "C#");
        m.put("javascript",     "JavaScript");
        m.put("typescript",     "TypeScript");
        m.put("kotlin",         "Kotlin");
        m.put("swift",          "Swift");
        m.put("golang",         "Go");
        m.put("rust",           "Rust");
        m.put("php",            "PHP");
        m.put("ruby",           "Ruby");
        m.put("scala",          "Scala");
        m.put("perl",           "Perl");
        m.put("bash",           "Bash");
        m.put("shell",          "Shell Scripting");
        m.put("shell scripting","Shell Scripting");
        m.put("powershell",     "PowerShell");
        m.put("dart",           "Dart");
        m.put("matlab",         "MATLAB");

        // --- Web Frameworks & Libraries ---
        m.put("spring boot",    "Spring Boot");
        m.put("spring",         "Spring Framework");
        m.put("spring framework","Spring Framework");
        m.put("spring security","Spring Security");
        m.put("spring mvc",     "Spring MVC");
        m.put("hibernate",      "Hibernate");
        m.put("jpa",            "JPA");
        m.put("jdbc",           "JDBC");
        m.put("maven",          "Maven");
        m.put("gradle",         "Gradle");
        m.put("react",          "React");
        m.put("reactjs",        "React");
        m.put("react.js",       "React");
        m.put("angular",        "Angular");
        m.put("vue",            "Vue.js");
        m.put("vue.js",         "Vue.js");
        m.put("node.js",        "Node.js");
        m.put("nodejs",         "Node.js");
        m.put("express",        "Express.js");
        m.put("express.js",     "Express.js");
        m.put("next.js",        "Next.js");
        m.put("nextjs",         "Next.js");
        m.put("nuxt.js",        "Nuxt.js");
        m.put("flask",          "Flask");
        m.put("django",         "Django");
        m.put("fastapi",        "FastAPI");
        m.put("html",           "HTML");
        m.put("html5",          "HTML");
        m.put("css",            "CSS");
        m.put("css3",           "CSS");
        m.put("bootstrap",      "Bootstrap");
        m.put("tailwind",       "Tailwind CSS");
        m.put("tailwindcss",    "Tailwind CSS");
        m.put("tailwind css",   "Tailwind CSS");
        m.put("thymeleaf",      "Thymeleaf");
        m.put("jsp",            "JSP");
        m.put("servlet",        "Java Servlets");
        m.put("servlets",       "Java Servlets");
        m.put("mern",           "MERN Stack");
        m.put("mern stack",     "MERN Stack");

        // --- Databases ---
        m.put("mysql",          "MySQL");
        m.put("postgresql",     "PostgreSQL");
        m.put("postgres",       "PostgreSQL");
        m.put("mongodb",        "MongoDB");
        m.put("oracle",         "Oracle DB");
        m.put("sqlite",         "SQLite");
        m.put("redis",          "Redis");
        m.put("cassandra",      "Cassandra");
        m.put("elasticsearch",  "Elasticsearch");
        m.put("nosql",          "NoSQL");
        m.put("sql",            "SQL");
        m.put("firebase",       "Firebase");
        m.put("dynamodb",       "DynamoDB");
        m.put("mariadb",        "MariaDB");

        // --- Cloud & DevOps ---
        m.put("aws",            "AWS");
        m.put("amazon web services","AWS");
        m.put("azure",          "Azure");
        m.put("microsoft azure","Azure");
        m.put("gcp",            "GCP");
        m.put("google cloud",   "Google Cloud");
        m.put("docker",         "Docker");
        m.put("kubernetes",     "Kubernetes");
        m.put("k8s",            "Kubernetes");
        m.put("linux",          "Linux");
        m.put("ubuntu",         "Ubuntu");
        m.put("git",            "Git");
        m.put("github",         "GitHub");
        m.put("gitlab",         "GitLab");
        m.put("bitbucket",      "Bitbucket");
        m.put("jenkins",        "Jenkins");
        m.put("ci/cd",          "CI/CD");
        m.put("terraform",      "Terraform");
        m.put("ansible",        "Ansible");

        // --- APIs & Architecture ---
        m.put("rest api",       "REST API");
        m.put("restful",        "REST API");
        m.put("restful apis",   "REST API");
        m.put("graphql",        "GraphQL");
        m.put("jwt",            "JWT");
        m.put("oauth",          "OAuth");
        m.put("microservices",  "Microservices");
        m.put("kafka",          "Apache Kafka");
        m.put("apache kafka",   "Apache Kafka");
        m.put("rabbitmq",       "RabbitMQ");
        m.put("swagger",        "Swagger");
        m.put("postman",        "Postman");

        // --- Testing ---
        m.put("junit",          "JUnit");
        m.put("mockito",        "Mockito");
        m.put("selenium",       "Selenium");
        m.put("jmeter",         "JMeter");

        // --- AI / ML / Data ---
        m.put("tensorflow",          "TensorFlow");
        m.put("pytorch",             "PyTorch");
        m.put("scikit-learn",        "Scikit-Learn");
        m.put("scikit learn",        "Scikit-Learn");
        m.put("sklearn",             "Scikit-Learn");
        m.put("pandas",              "Pandas");
        m.put("numpy",               "NumPy");
        m.put("keras",               "Keras");
        m.put("opencv",              "OpenCV");
        m.put("openai",              "OpenAI");
        m.put("openai api",          "OpenAI API");
        m.put("llm",                 "LLM");
        m.put("langchain",           "LangChain");
        m.put("prompt engineering",  "Prompt Engineering");
        m.put("machine learning",    "Machine Learning");
        m.put("deep learning",       "Deep Learning");
        m.put("artificial intelligence","Artificial Intelligence");
        m.put("nlp",                 "NLP");
        m.put("natural language processing","NLP");
        m.put("computer vision",     "Computer Vision");
        m.put("data science",        "Data Science");
        m.put("data analysis",       "Data Analysis");
        m.put("power bi",            "Power BI");
        m.put("tableau",             "Tableau");

        // --- Cyber Security ---
        m.put("cyber security",         "Cyber Security");
        m.put("cybersecurity",          "Cyber Security");
        m.put("ethical hacking",        "Ethical Hacking");
        m.put("penetration testing",    "Penetration Testing");
        m.put("pen testing",            "Penetration Testing");
        m.put("nmap",                   "Nmap");
        m.put("wireshark",              "Wireshark");
        m.put("burp suite",             "Burp Suite");
        m.put("burpsuite",              "Burp Suite");
        m.put("metasploit",             "Metasploit");
        m.put("owasp",                  "OWASP");
        m.put("dirbuster",              "DirBuster");
        m.put("kali linux",             "Kali Linux");
        m.put("network security",       "Network Security");
        m.put("siem",                   "SIEM");
        m.put("vulnerability assessment","Vulnerability Assessment");
        m.put("cryptography",           "Cryptography");
        m.put("firewall",               "Firewall");
        m.put("ids/ips",                "IDS/IPS");

        // --- Mobile ---
        m.put("android",        "Android");
        m.put("flutter",        "Flutter");
        m.put("react native",   "React Native");
        m.put("ios",            "iOS");

        return m;
    }
}
