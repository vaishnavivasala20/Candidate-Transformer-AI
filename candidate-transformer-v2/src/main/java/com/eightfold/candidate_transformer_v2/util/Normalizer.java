package com.eightfold.candidate_transformer_v2.util;

import java.util.Map;

/**
 * Normalizes candidate data fields: names, emails, phones, and skill names.
 */
public class Normalizer {

    private static final Map<String, String> SKILL_MAP = buildSkillMap();

    public String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return "";
        return email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String cleaned = phone.replaceAll("[^0-9+]", "");
        if (cleaned.startsWith("+")) return cleaned;
        if (cleaned.length() == 10) return "+91" + cleaned;
        return cleaned;
    }

    public String normalizeName(String name) {
        if (name == null || name.isBlank()) return "";
        String[] words = name.trim().toLowerCase().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            builder.append(Character.toUpperCase(word.charAt(0)))
                   .append(word.substring(1))
                   .append(" ");
        }
        return builder.toString().trim();
    }

    /**
     * Normalizes a skill name to its canonical form.
     * Skills already given canonical names by ResumeParser pass through unchanged.
     * Additional alias resolution is handled here for CSV-sourced skills.
     */
    public String normalizeSkill(String skill) {
        if (skill == null || skill.isBlank()) return "";
        String lower = skill.trim().toLowerCase();
        if (SKILL_MAP.containsKey(lower)) return SKILL_MAP.get(lower);
        // Title-case fallback
        String[] words = lower.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private static Map<String, String> buildSkillMap() {
        return Map.ofEntries(
            Map.entry("js", "JavaScript"),
            Map.entry("javascript", "JavaScript"),
            Map.entry("typescript", "TypeScript"),
            Map.entry("ts", "TypeScript"),
            Map.entry("springboot", "Spring Boot"),
            Map.entry("spring boot", "Spring Boot"),
            Map.entry("spring", "Spring Framework"),
            Map.entry("reactjs", "React"),
            Map.entry("react.js", "React"),
            Map.entry("react", "React"),
            Map.entry("nodejs", "Node.js"),
            Map.entry("node.js", "Node.js"),
            Map.entry("node", "Node.js"),
            Map.entry("expressjs", "Express.js"),
            Map.entry("express.js", "Express.js"),
            Map.entry("express", "Express.js"),
            Map.entry("nextjs", "Next.js"),
            Map.entry("next.js", "Next.js"),
            Map.entry("mongodb", "MongoDB"),
            Map.entry("mysql", "MySQL"),
            Map.entry("postgresql", "PostgreSQL"),
            Map.entry("postgres", "PostgreSQL"),
            Map.entry("html5", "HTML"),
            Map.entry("css3", "CSS"),
            Map.entry("tailwindcss", "Tailwind CSS"),
            Map.entry("tailwind", "Tailwind CSS"),
            Map.entry("python", "Python"),
            Map.entry("java", "Java"),
            Map.entry("kotlin", "Kotlin"),
            Map.entry("aws", "AWS"),
            Map.entry("docker", "Docker"),
            Map.entry("kubernetes", "Kubernetes"),
            Map.entry("k8s", "Kubernetes"),
            Map.entry("git", "Git"),
            Map.entry("github", "GitHub"),
            Map.entry("linux", "Linux"),
            Map.entry("rest api", "REST API"),
            Map.entry("restful", "REST API"),
            Map.entry("jwt", "JWT"),
            Map.entry("tensorflow", "TensorFlow"),
            Map.entry("pytorch", "PyTorch"),
            Map.entry("scikit-learn", "Scikit-Learn"),
            Map.entry("scikit learn", "Scikit-Learn"),
            Map.entry("pandas", "Pandas"),
            Map.entry("numpy", "NumPy"),
            Map.entry("cybersecurity", "Cyber Security"),
            Map.entry("cyber security", "Cyber Security"),
            Map.entry("nmap", "Nmap"),
            Map.entry("wireshark", "Wireshark"),
            Map.entry("burpsuite", "Burp Suite"),
            Map.entry("burp suite", "Burp Suite"),
            Map.entry("metasploit", "Metasploit"),
            Map.entry("owasp", "OWASP")
        );
    }
}
