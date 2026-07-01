package com.eightfold.candidate_transformer_v2.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.model.Provenance;
import com.eightfold.candidate_transformer_v2.model.Skill;

public class Merger {

    public Candidate merge(Candidate csvCandidate, Candidate resumeCandidate) {

        Candidate merged = Candidate.builder()
                .candidateId(csvCandidate.getCandidateId() != null
                        ? csvCandidate.getCandidateId()
                        : resumeCandidate.getCandidateId())

                .fullName(preferLonger(csvCandidate.getFullName(), resumeCandidate.getFullName()))

                .emails(mergeStrings(csvCandidate.getEmails(), resumeCandidate.getEmails()))

                .phones(mergeStrings(csvCandidate.getPhones(), resumeCandidate.getPhones()))

                .location(preferNonEmpty(csvCandidate.getLocation(), resumeCandidate.getLocation()))

                .headline(preferNonEmpty(csvCandidate.getHeadline(), resumeCandidate.getHeadline()))

                // Always prefer resume-parsed yearsExperience — CSV rarely contains this
                .yearsExperience(resumeCandidate.getYearsExperience() != null
                        ? resumeCandidate.getYearsExperience()
                        : csvCandidate.getYearsExperience())

                .skills(mergeSkills(csvCandidate.getSkills(), resumeCandidate.getSkills()))

                // Education and experience always come from Resume — CSV doesn't contain these
                .education(resumeCandidate.getEducation().isEmpty()
                        ? csvCandidate.getEducation()
                        : resumeCandidate.getEducation())

                .experience(resumeCandidate.getExperience().isEmpty()
                        ? csvCandidate.getExperience()
                        : resumeCandidate.getExperience())

                .projects(resumeCandidate.getProjects().isEmpty()
                        ? csvCandidate.getProjects()
                        : resumeCandidate.getProjects())

                .certifications(resumeCandidate.getCertifications().isEmpty()
                        ? csvCandidate.getCertifications()
                        : resumeCandidate.getCertifications())

                .provenance(buildProvenance())

                .overallConfidence(0.95)

                .build();

        return merged;
    }

    private List<Provenance> buildProvenance() {

        List<Provenance> list = new ArrayList<>();

        list.add(create("fullName", "CSV", "Direct Mapping"));
        list.add(create("email", "CSV", "Normalization"));
        list.add(create("phone", "CSV", "Normalization"));
        list.add(create("location", "CSV", "Direct Mapping"));
        list.add(create("headline", "CSV", "Direct Mapping"));

        list.add(create("skills", "Resume", "PDF Parsing"));
        list.add(create("experience", "Resume", "PDF Parsing"));
        list.add(create("education", "Resume", "PDF Parsing"));

        return list;
    }

    private Provenance create(String field, String source, String method) {

        return Provenance.builder()
                .field(field)
                .source(source)
                .method(method)
                .confidence(0.95)
                .build();
    }

    private String preferLonger(String first, String second) {

        if (first == null || first.isBlank())
            return second;

        if (second == null || second.isBlank())
            return first;

        return first.length() >= second.length() ? first : second;
    }

    private String preferNonEmpty(String first, String second) {

        if (first != null && !first.isBlank()) {
            return first;
        }

        return second;
    }

    private List<String> mergeStrings(List<String> first, List<String> second) {

        Map<String, String> unique = new LinkedHashMap<>();

        if (first != null) {
            for (String value : first) {
                if (value != null && !value.isBlank()) {
                    unique.put(value.toLowerCase(), value);
                }
            }
        }

        if (second != null) {
            for (String value : second) {
                if (value != null && !value.isBlank()) {
                    unique.putIfAbsent(value.toLowerCase(), value);
                }
            }
        }

        return new ArrayList<>(unique.values());
    }

    private List<Skill> mergeSkills(List<Skill> first, List<Skill> second) {

        Map<String, Skill> unique = new LinkedHashMap<>();

        if (first != null) {

            for (Skill skill : first) {

                if (skill != null && skill.getName() != null) {

                    unique.put(skill.getName().toLowerCase(), skill);

                }
            }

        }

        if (second != null) {

            for (Skill skill : second) {

                if (skill != null && skill.getName() != null) {

                    unique.putIfAbsent(skill.getName().toLowerCase(), skill);

                }

            }

        }

        return new ArrayList<>(unique.values());

    }

}