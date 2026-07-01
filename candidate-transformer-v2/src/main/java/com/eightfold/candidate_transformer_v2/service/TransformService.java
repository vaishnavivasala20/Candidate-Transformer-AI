package com.eightfold.candidate_transformer_v2.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.eightfold.candidate_transformer_v2.ai.AIAnalyzer;
import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.model.RecruiterInsights;
import com.eightfold.candidate_transformer_v2.model.Skill;
import com.eightfold.candidate_transformer_v2.parser.CsvParser;
import com.eightfold.candidate_transformer_v2.parser.ResumeParser;
import com.eightfold.candidate_transformer_v2.util.Merger;
import com.eightfold.candidate_transformer_v2.util.Normalizer;

@Service
public class TransformService {

    private final CsvParser csvParser = new CsvParser();

    private final ResumeParser resumeParser = new ResumeParser();

    private final Normalizer normalizer = new Normalizer();

    private final Merger merger = new Merger();

    private RecruiterInsights recruiterInsights;


    private final AIAnalyzer aiAnalyzer = new AIAnalyzer();


    public Candidate transform(MultipartFile csvFile,
                               MultipartFile resumeFile) throws IOException {

        Candidate csvCandidate =
                csvParser.parseFirstCandidate(csvFile);

        Candidate resumeCandidate =
                resumeParser.parseCandidate(resumeFile);

        normalize(csvCandidate);

        normalize(resumeCandidate);

        Candidate merged =
                merger.merge(csvCandidate, resumeCandidate);

        merged.setCandidateId(UUID.randomUUID().toString());

        merged.setRecruiterInsights(
        aiAnalyzer.analyze(merged));

        

        return merged;
    }

    private void normalize(Candidate candidate) {

        if (candidate == null) {
            return;
        }

        candidate.setFullName(
                normalizer.normalizeName(candidate.getFullName()));

        List<String> emails = new ArrayList<>();

        for (String email : candidate.getEmails()) {
            emails.add(normalizer.normalizeEmail(email));
        }

        candidate.setEmails(emails);

        List<String> phones = new ArrayList<>();

        for (String phone : candidate.getPhones()) {
            phones.add(normalizer.normalizePhone(phone));
        }

        candidate.setPhones(phones);

        List<Skill> normalizedSkills = new ArrayList<>();

        for (Skill skill : candidate.getSkills()) {

            skill.setName(
                    normalizer.normalizeSkill(skill.getName()));

            normalizedSkills.add(skill);
        }

        candidate.setSkills(normalizedSkills);
    }
}