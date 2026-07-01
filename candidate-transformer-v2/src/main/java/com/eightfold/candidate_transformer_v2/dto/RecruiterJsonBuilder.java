package com.eightfold.candidate_transformer_v2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.AiInsights;
import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.CandidateProfile;
import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.ContactInformation;
import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.EducationEntry;
import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.ExperienceEntry;
import com.eightfold.candidate_transformer_v2.dto.CandidateReportDto.ProjectEntry;
import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.model.Education;
import com.eightfold.candidate_transformer_v2.model.Experience;
import com.eightfold.candidate_transformer_v2.model.Project;
import com.eightfold.candidate_transformer_v2.model.RecruiterInsights;
import com.eightfold.candidate_transformer_v2.model.Skill;

/**
 * Builds a professional recruiter-friendly {@link CandidateReportDto}
 * from the internal {@link Candidate} domain object.
 *
 * Rules enforced:
 *  - No null values — replaced with descriptive "Not Available" strings.
 *  - Empty arrays replaced with readable strings ("No Projects Available", etc.).
 *  - Scores formatted as "92%" strings, years as "3 Years".
 *  - No internal Java class structure exposed in output.
 */
public class RecruiterJsonBuilder {

    private static final String NOT_AVAILABLE              = "Not Available";
    private static final String NO_EXPERIENCE              = "No Professional Experience";
    private static final String NO_PROJECTS                = "No Projects Available";
    private static final String NO_CERTIFICATIONS          = "No Certifications Available";
    private static final String NO_SKILLS                  = "No Technical Skills Found";
    private static final String NO_EDUCATION               = "No Education Information Available";

    public CandidateReportDto build(Candidate c) {
        CandidateReportDto dto = new CandidateReportDto();
        dto.candidateProfile    = buildProfile(c);
        dto.contactInformation  = buildContact(c);
        dto.technicalSkills     = buildSkills(c);
        dto.education           = buildEducation(c);
        dto.professionalExperience = buildExperience(c);
        dto.projects            = buildProjects(c);
        dto.certifications      = buildCertifications(c);
        dto.aiRecruiterInsights = buildInsights(c);
        return dto;
    }

    // -----------------------------------------------------------------------
    // Candidate Profile
    // -----------------------------------------------------------------------
    private CandidateProfile buildProfile(Candidate c) {
        CandidateProfile p = new CandidateProfile();
        p.candidateId   = val(c.getCandidateId());
        p.fullName      = val(c.getFullName());

        RecruiterInsights ri = c.getRecruiterInsights();
        p.headline      = val(c.getHeadline(),
                              ri != null ? ri.getRecommendedRole() : null);

        Integer yrs = c.getYearsExperience();
        boolean isFresher = (yrs == null || yrs == 0);
        p.candidateType     = isFresher ? "Fresher" : "Experienced";
        p.yearsOfExperience = isFresher ? "0 Years"
                              : yrs + (yrs == 1 ? " Year" : " Years");

        p.professionalSummary = val(c.getSummary());

        if (ri != null) {
            p.matchScore          = (int) ri.getMatchScore() + "%";
            p.profileCompleteness = ri.getProfileCompleteness() + "%";
        } else {
            p.matchScore          = NOT_AVAILABLE;
            p.profileCompleteness = NOT_AVAILABLE;
        }
        return p;
    }

    // -----------------------------------------------------------------------
    // Contact
    // -----------------------------------------------------------------------
    private ContactInformation buildContact(Candidate c) {
        ContactInformation ci = new ContactInformation();
        ci.email    = firstOrNA(c.getEmails());
        ci.phone    = firstOrNA(c.getPhones());
        ci.location = val(c.getLocation());
        return ci;
    }

    // -----------------------------------------------------------------------
    // Technical Skills
    // -----------------------------------------------------------------------
    private Object buildSkills(Candidate c) {
        if (c.getSkills() == null || c.getSkills().isEmpty()) return NO_SKILLS;
        List<String> names = c.getSkills().stream()
                .map(Skill::getName)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.toList());
        return names.isEmpty() ? NO_SKILLS : names;
    }

    // -----------------------------------------------------------------------
    // Education
    // -----------------------------------------------------------------------
    private Object buildEducation(Candidate c) {
        if (c.getEducation() == null || c.getEducation().isEmpty()) return NO_EDUCATION;
        List<EducationEntry> list = new ArrayList<>();
        for (Education edu : c.getEducation()) {
            EducationEntry e = new EducationEntry();
            e.degree         = val(edu.getDegree());
            e.specialization = val(edu.getField());
            e.institution    = val(edu.getInstitution());
            e.graduationYear = edu.getEndYear() != null
                               ? String.valueOf(edu.getEndYear()) : NOT_AVAILABLE;
            list.add(e);
        }
        return list.isEmpty() ? NO_EDUCATION : list;
    }

    // -----------------------------------------------------------------------
    // Professional Experience
    // -----------------------------------------------------------------------
    private Object buildExperience(Candidate c) {
        if (c.getExperience() == null || c.getExperience().isEmpty()) return NO_EXPERIENCE;
        List<ExperienceEntry> list = new ArrayList<>();
        for (Experience exp : c.getExperience()) {
            ExperienceEntry e = new ExperienceEntry();
            e.designation = val(exp.getTitle());
            e.company     = val(exp.getCompany());
            e.duration    = buildDuration(exp.getStartDate(), exp.getEndDate());
            e.summary     = val(exp.getSummary());
            list.add(e);
        }
        return list.isEmpty() ? NO_EXPERIENCE : list;
    }

    // -----------------------------------------------------------------------
    // Projects
    // -----------------------------------------------------------------------
    private Object buildProjects(Candidate c) {
        if (c.getProjects() == null || c.getProjects().isEmpty()) return NO_PROJECTS;
        List<ProjectEntry> list = new ArrayList<>();
        for (Project proj : c.getProjects()) {
            ProjectEntry p = new ProjectEntry();
            p.projectName = val(proj.getProjectName());
            p.description = val(proj.getDescription());
            List<String> techs = proj.getTechnologiesUsed();
            p.technologiesUsed = (techs != null && !techs.isEmpty()) ? techs : "Not Specified";
            list.add(p);
        }
        return list.isEmpty() ? NO_PROJECTS : list;
    }

    // -----------------------------------------------------------------------
    // Certifications
    // -----------------------------------------------------------------------
    private Object buildCertifications(Candidate c) {
        if (c.getCertifications() == null || c.getCertifications().isEmpty())
            return NO_CERTIFICATIONS;
        List<String> filtered = c.getCertifications().stream()
                .filter(cert -> cert != null && !cert.isBlank())
                .collect(Collectors.toList());
        return filtered.isEmpty() ? NO_CERTIFICATIONS : filtered;
    }

    // -----------------------------------------------------------------------
    // AI Insights
    // -----------------------------------------------------------------------
    private AiInsights buildInsights(Candidate c) {
        RecruiterInsights ri = c.getRecruiterInsights();
        AiInsights ai = new AiInsights();

        if (ri == null) {
            ai.candidateStrengths  = "No Analysis Available";
            ai.areasForImprovement = "No Analysis Available";
            ai.recommendedRole     = NOT_AVAILABLE;
            ai.profileCompleteness = NOT_AVAILABLE;
            ai.candidateReadiness  = NOT_AVAILABLE;
            ai.overallRecommendation = NOT_AVAILABLE;
            ai.overallAssessment   = NOT_AVAILABLE;
            return ai;
        }

        List<String> strengths = ri.getStrengths();
        ai.candidateStrengths = (strengths != null && !strengths.isEmpty())
                ? strengths : "No Specific Strengths Detected";

        List<String> missing = ri.getMissingSkills();
        ai.areasForImprovement = (missing != null && !missing.isEmpty())
                ? missing : "No Critical Gaps Identified";

        ai.recommendedRole     = val(ri.getRecommendedRole());
        ai.profileCompleteness = ri.getProfileCompleteness() + "%";

        double score = ri.getMatchScore();
        if      (score >= 80) ai.candidateReadiness = "Ready for Technical Interview";
        else if (score >= 60) ai.candidateReadiness = "Ready for HR Screening";
        else if (score >= 40) ai.candidateReadiness = "Needs Skill Assessment";
        else                  ai.candidateReadiness = "Profile Incomplete";

        ai.overallRecommendation = val(ri.getRecommendation());
        ai.overallAssessment     = buildOverallAssessment(c, ri, score);
        return ai;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private String val(String... candidates) {
        for (String s : candidates) {
            if (s != null && !s.isBlank()) return s;
        }
        return NOT_AVAILABLE;
    }

    private String firstOrNA(List<String> list) {
        if (list == null || list.isEmpty()) return NOT_AVAILABLE;
        String v = list.get(0);
        return (v != null && !v.isBlank()) ? v : NOT_AVAILABLE;
    }

    private String buildDuration(String start, String end) {
        boolean hasStart = start != null && !start.isBlank();
        boolean hasEnd   = end   != null && !end.isBlank();
        if (!hasStart && !hasEnd) return NOT_AVAILABLE;
        return (hasStart ? start : "?") + " \u2013 " + (hasEnd ? end : "Present");
    }

    private String buildOverallAssessment(Candidate c, RecruiterInsights ri, double score) {
        int skillCount = c.getSkills() != null ? c.getSkills().size() : 0;
        int yrs        = c.getYearsExperience() != null ? c.getYearsExperience() : 0;
        String role    = ri.getRecommendedRole() != null ? ri.getRecommendedRole() : "Software Engineer";

        StringBuilder sb = new StringBuilder();
        sb.append(role).append(" profile");
        if (skillCount > 0)
            sb.append(" with ").append(skillCount).append(" verified skill").append(skillCount == 1 ? "" : "s");
        sb.append(". ");
        if (yrs > 0)
            sb.append(yrs).append(" year").append(yrs == 1 ? "" : "s").append(" of professional experience. ");
        else
            sb.append("Fresher / Entry-level profile. ");
        if (c.getEducation() != null && !c.getEducation().isEmpty()) {
            String deg = c.getEducation().get(0).getDegree();
            if (deg != null && !deg.isBlank())
                sb.append("Education: ").append(deg).append(". ");
        }
        sb.append("Overall Match Score: ").append((int) score).append("%.");
        return sb.toString();
    }
}
