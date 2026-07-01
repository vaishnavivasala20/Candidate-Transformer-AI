package com.eightfold.candidate_transformer_v2.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Professional ATS-style candidate report for recruiter JSON export.
 *
 * Designed so a recruiter with no technical knowledge can open this file
 * in any text editor and immediately understand the candidate's profile.
 *
 * Rules:
 * - No internal Java types exposed.
 * - No null values (replaced with "Not Available" strings).
 * - No empty arrays (replaced with descriptive strings).
 * - All sections are clearly named and logically grouped.
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "candidateProfile",
    "contactInformation",
    "technicalSkills",
    "education",
    "professionalExperience",
    "projects",
    "certifications",
    "aiRecruiterInsights"
})
public class CandidateReportDto {

    @JsonProperty("candidateProfile")
    public CandidateProfile candidateProfile;

    @JsonProperty("contactInformation")
    public ContactInformation contactInformation;

    @JsonProperty("technicalSkills")
    public Object technicalSkills;          // List<String> or "No Technical Skills Found"

    @JsonProperty("education")
    public Object education;               // List<EducationEntry> or "No Education Information Available"

    @JsonProperty("professionalExperience")
    public Object professionalExperience;  // List<ExperienceEntry> or "No Professional Experience"

    @JsonProperty("projects")
    public Object projects;                // List<ProjectEntry> or "No Projects Available"

    @JsonProperty("certifications")
    public Object certifications;          // List<String> or "No Certifications Available"

    @JsonProperty("aiRecruiterInsights")
    public AiInsights aiRecruiterInsights;

    // -----------------------------------------------------------------------
    // Nested: Candidate Profile
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({
        "candidateId", "fullName", "headline", "candidateType",
        "yearsOfExperience", "professionalSummary", "matchScore", "profileCompleteness"
    })
    public static class CandidateProfile {
        @JsonProperty("candidateId")          public String candidateId;
        @JsonProperty("fullName")             public String fullName;
        @JsonProperty("headline")             public String headline;
        @JsonProperty("candidateType")        public String candidateType;
        @JsonProperty("yearsOfExperience")    public String yearsOfExperience;
        @JsonProperty("professionalSummary")  public String professionalSummary;
        @JsonProperty("matchScore")           public String matchScore;
        @JsonProperty("profileCompleteness")  public String profileCompleteness;
    }

    // -----------------------------------------------------------------------
    // Nested: Contact
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({ "email", "phone", "location" })
    public static class ContactInformation {
        @JsonProperty("email")    public String email;
        @JsonProperty("phone")    public String phone;
        @JsonProperty("location") public String location;
    }

    // -----------------------------------------------------------------------
    // Nested: Experience
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({ "designation", "company", "duration", "summary" })
    public static class ExperienceEntry {
        @JsonProperty("designation") public String designation;
        @JsonProperty("company")     public String company;
        @JsonProperty("duration")    public String duration;
        @JsonProperty("summary")     public String summary;
    }

    // -----------------------------------------------------------------------
    // Nested: Education
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({ "degree", "specialization", "institution", "graduationYear" })
    public static class EducationEntry {
        @JsonProperty("degree")          public String degree;
        @JsonProperty("specialization")  public String specialization;
        @JsonProperty("institution")     public String institution;
        @JsonProperty("graduationYear")  public String graduationYear;
    }

    // -----------------------------------------------------------------------
    // Nested: Project
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({ "projectName", "technologiesUsed", "description" })
    public static class ProjectEntry {
        @JsonProperty("projectName")      public String projectName;
        @JsonProperty("technologiesUsed") public Object technologiesUsed; // List<String> or "Not Specified"
        @JsonProperty("description")      public String description;
    }

    // -----------------------------------------------------------------------
    // Nested: AI Insights
    // -----------------------------------------------------------------------
    @JsonInclude(Include.NON_NULL)
    @JsonPropertyOrder({
        "candidateStrengths", "areasForImprovement", "recommendedRole",
        "profileCompleteness", "candidateReadiness",
        "overallRecommendation", "overallAssessment"
    })
    public static class AiInsights {
        @JsonProperty("candidateStrengths")    public Object candidateStrengths;  // List<String> or string
        @JsonProperty("areasForImprovement")   public Object areasForImprovement; // List<String> or string
        @JsonProperty("recommendedRole")       public String recommendedRole;
        @JsonProperty("profileCompleteness")   public String profileCompleteness;
        @JsonProperty("candidateReadiness")    public String candidateReadiness;
        @JsonProperty("overallRecommendation") public String overallRecommendation;
        @JsonProperty("overallAssessment")     public String overallAssessment;
    }
}
