package com.eightfold.candidate_transformer_v2.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.model.Education;
import com.eightfold.candidate_transformer_v2.model.Experience;
import com.eightfold.candidate_transformer_v2.model.RecruiterInsights;
import com.eightfold.candidate_transformer_v2.model.Skill;

/**
 * Data-driven AI analyzer.
 * All outputs are derived entirely from parsed candidate data.
 * No hardcoded recommendations or assumed roles.
 */
public class AIAnalyzer {

    // -----------------------------------------------------------------------
    // Role detection skill sets
    // -----------------------------------------------------------------------

    private static final Set<String> JAVA_SKILLS = keywords(
            "java", "spring boot", "spring framework", "spring security", "jpa/hibernate",
            "java servlets", "jdbc", "maven", "gradle", "junit", "mockito", "microservices"
    );

    private static final Set<String> FRONTEND_SKILLS = keywords(
            "react", "angular", "vue.js", "html", "css", "javascript", "typescript",
            "bootstrap", "tailwind css", "next.js"
    );

    private static final Set<String> BACKEND_SKILLS = keywords(
            "node.js", "express.js", "flask", "django", "fastapi", "rest api", "graphql"
    );

    private static final Set<String> FULLSTACK_SKILLS = keywords(
            "mern stack", "java", "spring boot", "react", "node.js", "mongodb"
    );

    private static final Set<String> DATA_SCIENCE_SKILLS = keywords(
            "python", "machine learning", "deep learning", "tensorflow", "pytorch",
            "scikit-learn", "pandas", "numpy", "data science", "data analysis",
            "nlp", "computer vision", "keras", "tableau", "power bi"
    );

    private static final Set<String> AI_SKILLS = keywords(
            "openai", "llm", "langchain", "prompt engineering", "tensorflow",
            "pytorch", "nlp", "machine learning"
    );

    private static final Set<String> CYBER_SKILLS = keywords(
            "cyber security", "nmap", "wireshark", "burp suite", "metasploit", "owasp",
            "penetration testing", "kali linux", "ethical hacking", "network security",
            "siem", "soc operations", "vulnerability assessment", "cryptography",
            "ids/ips", "firewall configuration"
    );

    private static final Set<String> DEVOPS_SKILLS = keywords(
            "docker", "kubernetes", "jenkins", "ci/cd", "terraform", "ansible",
            "aws", "azure", "gcp", "linux", "git"
    );

    private static final Set<String> ANDROID_SKILLS = keywords(
            "android", "kotlin", "flutter", "react native"
    );

    // -----------------------------------------------------------------------
    // Skills commonly expected for well-rounded profiles (gap analysis)
    // -----------------------------------------------------------------------

    private static final List<String[]> ROLE_EXPECTED_SKILLS = Arrays.asList(
            new String[]{"Java Backend", "Java", "Spring Boot", "MySQL", "REST API", "Git"},
            new String[]{"Frontend", "HTML", "CSS", "JavaScript", "React", "Git"},
            new String[]{"Full Stack", "Java", "Spring Boot", "React", "MySQL", "REST API"},
            new String[]{"Data Science", "Python", "Pandas", "NumPy", "Scikit-Learn", "SQL"},
            new String[]{"Cyber Security", "Nmap", "Wireshark", "Burp Suite", "OWASP", "Linux"},
            new String[]{"DevOps", "Docker", "Kubernetes", "Jenkins", "CI/CD", "Linux"},
            new String[]{"AI/ML Engineer", "Python", "TensorFlow", "PyTorch", "NLP", "Pandas"}
    );

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    public RecruiterInsights analyze(Candidate candidate) {
        Set<String> skillNames = extractSkillNames(candidate.getSkills());

        String role                = determineRole(skillNames, candidate);
        List<String> strengths     = buildStrengths(candidate, skillNames);
        List<String> missing       = buildMissingSkills(role, skillNames);
        int completeness           = computeCompleteness(candidate);
        double matchScore          = computeMatchScore(candidate, skillNames, completeness);
        String recommendation      = buildRecommendation(candidate, matchScore);
        String summary             = buildSummary(candidate, skillNames, role);

        candidate.setSummary(summary);

        return RecruiterInsights.builder()
                .strengths(strengths)
                .missingSkills(missing)
                .recommendedRole(role)
                .recommendation(recommendation)
                .profileCompleteness(completeness)
                .matchScore(matchScore)
                .build();
    }

    // -----------------------------------------------------------------------
    // Role determination — data driven
    // -----------------------------------------------------------------------

    private String determineRole(Set<String> skills, Candidate candidate) {
        int javaScore     = countMatches(skills, JAVA_SKILLS);
        int frontScore    = countMatches(skills, FRONTEND_SKILLS);
        int backScore     = countMatches(skills, BACKEND_SKILLS);
        int dataScore     = countMatches(skills, DATA_SCIENCE_SKILLS);
        int aiScore       = countMatches(skills, AI_SKILLS);
        int cyberScore    = countMatches(skills, CYBER_SKILLS);
        int devopsScore   = countMatches(skills, DEVOPS_SKILLS);
        int androidScore  = countMatches(skills, ANDROID_SKILLS);
        int fullScore     = countMatches(skills, FULLSTACK_SKILLS);

        // Determine role based on highest scoring category
        int max = Math.max(javaScore, Math.max(frontScore, Math.max(backScore,
                Math.max(dataScore, Math.max(aiScore, Math.max(cyberScore,
                        Math.max(devopsScore, Math.max(androidScore, fullScore))))))));

        if (max == 0) {
            // Fall back to headline from CSV if skills yield nothing
            String headline = candidate.getHeadline();
            return (headline != null && !headline.isBlank()) ? headline : "Software Engineer";
        }

        if (cyberScore == max) return "Cyber Security Engineer";
        if (aiScore == max && dataScore >= 2) return "AI/ML Engineer";
        if (dataScore == max) return "Data Science Engineer";
        if (devopsScore == max) return "DevOps Engineer";
        if (androidScore == max) return "Android Developer";
        if (fullScore >= 4 || (javaScore >= 2 && frontScore >= 2)) return "Full Stack Developer";
        if (javaScore == max) return "Java Backend Developer";
        if (frontScore == max) return "Frontend Developer";
        if (backScore == max) return "Backend Developer";

        return "Software Engineer";
    }

    // -----------------------------------------------------------------------
    // Strengths — derived from actual skills and profile data
    // -----------------------------------------------------------------------

    private List<String> buildStrengths(Candidate candidate, Set<String> skills) {
        List<String> strengths = new ArrayList<>();

        if (countMatches(skills, JAVA_SKILLS) >= 2) {
            strengths.add("Strong Java & Spring ecosystem proficiency");
        }
        if (countMatches(skills, FRONTEND_SKILLS) >= 2) {
            strengths.add("Solid frontend development skills");
        }
        if (countMatches(skills, BACKEND_SKILLS) >= 2) {
            strengths.add("Capable backend/API development");
        }
        if (countMatches(skills, DATA_SCIENCE_SKILLS) >= 2) {
            strengths.add("Machine Learning & Data Science knowledge");
        }
        if (countMatches(skills, AI_SKILLS) >= 2) {
            strengths.add("AI & LLM application experience");
        }
        if (countMatches(skills, CYBER_SKILLS) >= 2) {
            strengths.add("Cyber Security tools & techniques expertise");
        }
        if (countMatches(skills, DEVOPS_SKILLS) >= 2) {
            strengths.add("DevOps & Cloud infrastructure skills");
        }
        if (!candidate.getEducation().isEmpty()) {
            strengths.add("Formal academic qualification verified");
        }
        if (candidate.getYearsExperience() != null && candidate.getYearsExperience() > 0) {
            strengths.add(candidate.getYearsExperience() + "+ year(s) of professional experience");
        }
        if (candidate.getSkills().size() >= 8) {
            strengths.add("Broad technical skill set across multiple domains");
        }
        if (!candidate.getExperience().isEmpty()) {
            strengths.add("Practical hands-on work or internship experience");
        }

        if (strengths.isEmpty()) {
            strengths.add("Profile parsed successfully");
        }

        return strengths;
    }

    // -----------------------------------------------------------------------
    // Missing skills — based on expected skills for detected role
    // -----------------------------------------------------------------------

    private List<String> buildMissingSkills(String role, Set<String> skills) {
        for (String[] roleEntry : ROLE_EXPECTED_SKILLS) {
            if (role.toLowerCase().contains(roleEntry[0].toLowerCase())) {
                List<String> missing = new ArrayList<>();
                for (int i = 1; i < roleEntry.length; i++) {
                    if (!skills.contains(roleEntry[i].toLowerCase())) {
                        missing.add(roleEntry[i]);
                    }
                }
                return missing;
            }
        }
        return new ArrayList<>();
    }

    // -----------------------------------------------------------------------
    // Profile completeness score
    // -----------------------------------------------------------------------

    private int computeCompleteness(Candidate candidate) {
        int score = 0;
        if (hasValue(candidate.getFullName()))       score += 15;
        if (!candidate.getEmails().isEmpty())         score += 10;
        if (!candidate.getPhones().isEmpty())         score += 10;
        if (hasValue(candidate.getLocation()))        score += 5;
        if (hasValue(candidate.getHeadline()))        score += 5;
        if (!candidate.getSkills().isEmpty())         score += 20;
        if (!candidate.getEducation().isEmpty())      score += 15;
        if (!candidate.getExperience().isEmpty())     score += 15;
        if (candidate.getSkills().size() >= 5)        score += 5;
        return Math.min(score, 100);
    }

    // -----------------------------------------------------------------------
    // Match score — weighted combination
    // -----------------------------------------------------------------------

    private double computeMatchScore(Candidate candidate, Set<String> skills, int completeness) {
        double base = completeness;
        // Boost for number of recognized skills
        int skillBonus = Math.min(candidate.getSkills().size() * 2, 20);
        double raw = base + skillBonus;
        return Math.min(Math.round(raw), 100);
    }

    // -----------------------------------------------------------------------
    // Recruiter recommendation
    // -----------------------------------------------------------------------

    private String buildRecommendation(Candidate candidate, double matchScore) {
        boolean hasDegree     = !candidate.getEducation().isEmpty();
        boolean hasExperience = !candidate.getExperience().isEmpty();
        boolean hasSkills     = !candidate.getSkills().isEmpty();
        int years             = candidate.getYearsExperience() != null ? candidate.getYearsExperience() : 0;

        if (matchScore >= 80 && hasDegree && hasSkills) {
            if (years > 0) {
                return "Strong candidate. Proceed directly to technical interview.";
            }
            return "Promising fresher profile. Recommend technical screening round.";
        }
        if (matchScore >= 60) {
            if (hasExperience) {
                return "Candidate meets basic criteria. Schedule initial HR screening.";
            }
            return "Good academic profile. Suitable for junior or fresher positions.";
        }
        if (matchScore >= 40) {
            return "Partial profile match. Recommend further evaluation or skill assessment.";
        }
        return "Incomplete profile. Collect additional information before proceeding.";
    }

    // -----------------------------------------------------------------------
    // Dynamic professional summary
    // -----------------------------------------------------------------------

    private String buildSummary(Candidate candidate, Set<String> skills, String role) {
        StringBuilder sb = new StringBuilder();

        // Degree info
        String degreeStr = extractDegreeString(candidate);
        if (degreeStr != null) {
            sb.append(degreeStr);
        } else {
            sb.append("Candidate");
        }

        // Experience context
        int years = candidate.getYearsExperience() != null ? candidate.getYearsExperience() : 0;
        if (years > 0) {
            sb.append(" with ").append(years).append(" year").append(years > 1 ? "s" : "")
              .append(" of professional experience");
        } else if (!candidate.getExperience().isEmpty()) {
            sb.append(" with hands-on internship/project experience");
        } else {
            sb.append(" with strong academic foundation");
        }

        // Key skill highlights (top 5)
        List<String> topSkills = candidate.getSkills().stream()
                .limit(5)
                .map(Skill::getName)
                .collect(Collectors.toList());
        if (!topSkills.isEmpty()) {
            sb.append(", skilled in ").append(String.join(", ", topSkills));
        }

        // Role fit
        sb.append(". Suitable for ").append(role).append(" roles.");

        // Experience entries
        if (!candidate.getExperience().isEmpty()) {
            long internships = candidate.getExperience().stream()
                    .filter(e -> e.getTitle() != null &&
                            e.getTitle().toLowerCase().contains("intern"))
                    .count();
            if (internships > 0) {
                sb.append(" Completed ").append(internships)
                  .append(" internship").append(internships > 1 ? "s" : "").append(".");
            }
        }

        return sb.toString();
    }

    private String extractDegreeString(Candidate candidate) {
        if (candidate.getEducation().isEmpty()) return null;
        Education edu = candidate.getEducation().get(0);
        if (edu.getDegree() != null && !edu.getDegree().isBlank()) {
            return edu.getDegree() + " graduate";
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Set<String> extractSkillNames(List<Skill> skills) {
        Set<String> names = new LinkedHashSet<>();
        if (skills == null) return names;
        for (Skill s : skills) {
            if (s.getName() != null) {
                names.add(s.getName().toLowerCase());
            }
        }
        return names;
    }

    private int countMatches(Set<String> candidateSkills, Set<String> roleSkills) {
        int count = 0;
        for (String skill : roleSkills) {
            if (candidateSkills.contains(skill)) count++;
        }
        return count;
    }

    private boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }

    private static Set<String> keywords(String... values) {
        Set<String> set = new HashSet<>();
        for (String v : values) set.add(v.toLowerCase());
        return set;
    }
}
