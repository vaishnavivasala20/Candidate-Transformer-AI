package com.eightfold.candidate_transformer_v2.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Standalone PDF design document generator for Candidate Transformer AI.
 * Uses OpenPDF (com.lowagie) library.
 * Run via: mvnw exec:java -Dexec.mainClass="com.eightfold.candidate_transformer_v2.util.DesignDocGenerator"
 */
public class DesignDocGenerator {

    // ── Color Palette ──────────────────────────────────────────────────────────
    private static final Color COLOR_ACCENT      = new Color(37,  99,  235); // blue
    private static final Color COLOR_SECTION_BG  = new Color(239, 246, 255); // light blue
    private static final Color COLOR_TEXT        = new Color(17,  24,  39);  // near black
    private static final Color COLOR_MUTED       = new Color(107, 114, 128); // gray
    private static final Color COLOR_WHITE       = Color.WHITE;
    private static final Color COLOR_TABLE_BORDER= new Color(191, 219, 254); // blue-200
    private static final Color COLOR_ROW_ALT     = new Color(248, 250, 252); // slate-50

    // ── Fonts ──────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE       = new Font(Font.HELVETICA, 26, Font.BOLD,   COLOR_WHITE);
    private static final Font FONT_SUBTITLE    = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(191, 219, 254));
    private static final Font FONT_TAGS        = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(219, 234, 254));
    private static final Font FONT_SECTION     = new Font(Font.HELVETICA,  9, Font.BOLD,   COLOR_ACCENT);
    private static final Font FONT_BODY        = new Font(Font.HELVETICA,  8, Font.NORMAL, COLOR_TEXT);
    private static final Font FONT_BODY_BOLD   = new Font(Font.HELVETICA,  8, Font.BOLD,   COLOR_TEXT);
    private static final Font FONT_BULLET      = new Font(Font.HELVETICA,  8, Font.NORMAL, COLOR_TEXT);
    private static final Font FONT_MUTED       = new Font(Font.HELVETICA,  7, Font.NORMAL, COLOR_MUTED);
    private static final Font FONT_TABLE_HDR   = new Font(Font.HELVETICA,  8, Font.BOLD,   COLOR_WHITE);
    private static final Font FONT_TABLE_CELL  = new Font(Font.HELVETICA,  8, Font.NORMAL, COLOR_TEXT);
    private static final Font FONT_STEP        = new Font(Font.HELVETICA,  8, Font.BOLD,   COLOR_ACCENT);
    private static final Font FONT_FOOTER      = new Font(Font.HELVETICA,  7, Font.NORMAL, COLOR_MUTED);

    private static final String OUTPUT_PATH =
        "C:/Users/HP/Downloads/candidate-transformer-v2/candidate-transformer-v2/Candidate_Transformer_AI_Design_Document.pdf";

    // ──────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        System.out.println("Generating design document PDF...");
        new DesignDocGenerator().generate();
        System.out.println("PDF generated at: " + OUTPUT_PATH);
    }

    // ──────────────────────────────────────────────────────────────────────────
    public void generate() throws DocumentException, IOException {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(OUTPUT_PATH));

        // Footer event
        writer.setPageEvent(new FooterEvent());

        doc.open();

        // ── HEADER BLOCK (full width) ──────────────────────────────────────────
        addHeader(doc);

        // small gap
        doc.add(new Paragraph(" "));

        // ── TWO-COLUMN BODY ────────────────────────────────────────────────────
        PdfPTable twoCol = new PdfPTable(2);
        twoCol.setWidthPercentage(100);
        twoCol.setWidths(new float[]{1f, 1f});
        twoCol.setSpacingBefore(0);
        twoCol.getDefaultCell().setPadding(0);
        twoCol.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        PdfPCell leftCell  = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPadding(4);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPadding(4);

        // ── LEFT COLUMN ─────────────────────────────────────────────────────
        buildLeftColumn(leftCell);

        // ── RIGHT COLUMN ────────────────────────────────────────────────────
        buildRightColumn(rightCell);

        twoCol.addCell(leftCell);
        twoCol.addCell(rightCell);
        doc.add(twoCol);

        doc.close();
    }

    // ── HEADER ────────────────────────────────────────────────────────────────
    private void addHeader(Document doc) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_ACCENT);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(22);
        cell.setPaddingBottom(18);
        cell.setPaddingLeft(24);
        cell.setPaddingRight(24);

        // Title
        Paragraph title = new Paragraph("Candidate Transformer AI", FONT_TITLE);
        title.setSpacingAfter(4);
        cell.addElement(title);

        // Subtitle
        Paragraph subtitle = new Paragraph("AI-Powered Candidate Intelligence Platform", FONT_SUBTITLE);
        subtitle.setSpacingAfter(8);
        cell.addElement(subtitle);

        // Thin separator line (simulated with a small colored table)
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell(new Phrase(""));
        sepCell.setBackgroundColor(new Color(96, 165, 250));
        sepCell.setBorder(Rectangle.NO_BORDER);
        sepCell.setFixedHeight(1f);
        sep.addCell(sepCell);
        cell.addElement(sep);

        // Tags
        Paragraph tags = new Paragraph(
            "Spring Boot 3   ·   Java 21   ·   Apache PDFBox   ·   Thymeleaf   ·   Bootstrap 5",
            FONT_TAGS);
        tags.setSpacingBefore(7);
        cell.addElement(tags);

        header.addCell(cell);
        doc.add(header);
    }

    // ── LEFT COLUMN ───────────────────────────────────────────────────────────
    private void buildLeftColumn(PdfPCell col) throws DocumentException {

        // 1. PROJECT OBJECTIVE
        col.addElement(sectionHeader("1. PROJECT OBJECTIVE"));
        col.addElement(bodyPara(
            "Build an enterprise-grade ATS-style platform where recruiters upload a CSV and a " +
            "Resume PDF to auto-generate a unified, AI-analyzed candidate profile — ready for " +
            "sharing with hiring teams."));
        col.addElement(spacer(4));

        // 2. PROBLEM STATEMENT
        col.addElement(sectionHeader("2. PROBLEM STATEMENT"));
        col.addElement(bullet("Recruiters manually review resumes — slow and error-prone"));
        col.addElement(bullet("No unified view across CSV data and resume content"));
        col.addElement(bullet("Skills misidentified or fabricated by naive parsers"));
        col.addElement(bullet("No AI-driven gap analysis or role recommendation"));
        col.addElement(bullet("Raw JSON exports expose internal Java object structure"));
        col.addElement(spacer(4));

        // 3. TECHNOLOGY STACK (table)
        col.addElement(sectionHeader("3. TECHNOLOGY STACK"));
        col.addElement(buildTechTable());
        col.addElement(spacer(4));

        // 4. SYSTEM ARCHITECTURE
        col.addElement(sectionHeader("4. SYSTEM ARCHITECTURE"));
        col.addElement(labeledBody("Controllers:", "HomeController, TransformController, DownloadController"));
        col.addElement(labeledBody("Service Layer:", "TransformService (orchestration)"));
        col.addElement(labeledBody("Parsers:", "ResumeParser (section-based), CsvParser (header-mapped)"));
        col.addElement(labeledBody("Utilities:", "Merger, Normalizer"));
        col.addElement(labeledBody("AI Engine:", "AIAnalyzer (rule-based scoring)"));
        col.addElement(labeledBody("Models:", "Candidate, Skill, Experience, Education, Project, Certification"));
        col.addElement(labeledBody("DTOs:", "CandidateReportDto, RecruiterJsonBuilder"));
        col.addElement(labeledBody("Views:", "upload.html, index.html (Thymeleaf + Bootstrap 5)"));
        col.addElement(spacer(4));

        // 5. END-TO-END WORKFLOW
        col.addElement(sectionHeader("5. END-TO-END WORKFLOW"));
        col.addElement(stepPara("① Upload", "Recruiter CSV + Resume PDF via multipart form"));
        col.addElement(stepPara("② Parse", "CsvParser maps headers; ResumeParser segments sections"));
        col.addElement(stepPara("③ Normalize", "Email/phone/name/skill canonical forms"));
        col.addElement(stepPara("④ Merge", "CSV metadata + Resume content unified into Candidate"));
        col.addElement(stepPara("⑤ AI Analyze", "Role detection, strengths, gaps, match score, summary"));
        col.addElement(stepPara("⑥ Render", "Thymeleaf candidate profile report (index.html)"));
        col.addElement(stepPara("⑦ Export", "Recruiter-friendly JSON via DownloadController"));
        col.addElement(spacer(4));

        // 13. DESIGN DECISIONS
        col.addElement(sectionHeader("13. DESIGN DECISIONS"));
        col.addElement(bullet("Section-based parsing > full-text scanning (accuracy)"));
        col.addElement(bullet("Whitelist approach > NLP extraction (no hallucination)"));
        col.addElement(bullet("Rule-based AI > external API (zero latency, offline)"));
        col.addElement(bullet("DTO pattern > direct serialization (clean recruiter output)"));
        col.addElement(bullet("TreeSet for skills > ArrayList (auto-sort + deduplicate)"));
        col.addElement(bullet("Resume always preferred over CSV for structured data"));
        col.addElement(spacer(4));

        // 14. FUTURE IMPROVEMENTS
        col.addElement(sectionHeader("14. FUTURE IMPROVEMENTS"));
        col.addElement(bullet("LinkedIn profile URL parsing"));
        col.addElement(bullet("Multi-resume batch processing"));
        col.addElement(bullet("Real LLM integration (OpenAI GPT-4) for summary generation"));
        col.addElement(bullet("Resume scoring against a specific Job Description"));
        col.addElement(bullet("Export to PDF / DOCX candidate report"));
        col.addElement(bullet("Admin dashboard with candidate comparison"));
        col.addElement(bullet("Database persistence (PostgreSQL + Spring Data JPA)"));
        col.addElement(bullet("REST API for third-party ATS integration"));
    }

    // ── RIGHT COLUMN ──────────────────────────────────────────────────────────
    private void buildRightColumn(PdfPCell col) throws DocumentException {

        // 6. RESUME PARSING STRATEGY
        col.addElement(sectionHeader("6. RESUME PARSING STRATEGY"));
        col.addElement(subHeading("Section-Based Algorithm:"));
        col.addElement(bullet("Walk lines one-by-one detecting heading keywords"));
        col.addElement(bullet("Recognized headings: Skills, Experience, Education, Projects, Certifications"));
        col.addElement(bullet("Each section's content parsed independently"));
        col.addElement(bullet("Stop conditions prevent cross-section contamination"));
        col.addElement(subHeading("Skills Extraction (STRICT WHITELIST):"));
        col.addElement(bullet("ONLY the Skills / Technical Skills section is scanned"));
        col.addElement(bullet("Immediately stops at: Education, Experience, Projects, Certifications, Achievements"));
        col.addElement(bullet("~120-entry whitelist: aliases → canonical names"));
        col.addElement(bullet("Token matching: exact → strip parentheticals → strip versions"));
        col.addElement(bullet("Multi-word tokens (>5 words) rejected as sentences"));
        col.addElement(bullet("TreeSet ensures alphabetical sort + deduplication"));
        col.addElement(bullet("If no Skills section found → returns empty list (no guessing)"));
        col.addElement(spacer(4));

        // 7. CSV PARSING STRATEGY
        col.addElement(sectionHeader("7. CSV PARSING STRATEGY"));
        col.addElement(bullet("Apache Commons CSV with header-aware format"));
        col.addElement(bullet("Maps: fullName, email, phone, location, headline"));
        col.addElement(bullet("Graceful null handling via getValue() fallback"));
        col.addElement(bullet("Supports any column ordering via case-insensitive headers"));
        col.addElement(spacer(4));

        // 8. DATA NORMALIZATION
        col.addElement(sectionHeader("8. DATA NORMALIZATION"));
        col.addElement(bullet("Emails → lowercase, trimmed"));
        col.addElement(bullet("Phones → stripped of symbols; 10-digit → +91 prefix"));
        col.addElement(bullet("Names → Title Case word-by-word"));
        col.addElement(bullet("Skills → alias map resolves ~50 common variants"));
        col.addElement(spacer(4));

        // 9. CANDIDATE MERGE STRATEGY
        col.addElement(sectionHeader("9. CANDIDATE MERGE STRATEGY"));
        col.addElement(subHeading("Field Priority Rules:"));
        col.addElement(bullet("fullName → prefer longer string (CSV vs Resume)"));
        col.addElement(bullet("emails / phones → union, deduplicated by lowercase key"));
        col.addElement(bullet("headline / location → CSV preferred, Resume fallback"));
        col.addElement(bullet("skills → union via LinkedHashMap (CSV first, Resume deduplicates)"));
        col.addElement(bullet("education / experience / projects / certs → always Resume preferred"));
        col.addElement(bullet("yearsExperience → always from Resume (calculated from experience dates only)"));
        col.addElement(spacer(4));

        // 10. AI ANALYZER
        col.addElement(sectionHeader("10. AI ANALYZER — RULE-BASED ENGINE"));
        col.addElement(subHeading("Role Detection (9 domain clusters):"));
        col.addElement(bodyPara(
            "Java Backend · Frontend · Backend · Full Stack · Data Science · AI/ML · " +
            "Cyber Security · DevOps · Android"));
        col.addElement(bullet("Candidate's skills scored against each cluster"));
        col.addElement(bullet("Highest score determines recommended role"));
        col.addElement(bullet("Falls back to CSV headline if no match"));
        col.addElement(subHeading("Profile Completeness Scoring (max 100):"));
        col.addElement(bodyPara("Name +15, Email +10, Phone +10, Location +5, Headline +5, " +
            "Skills +20, Education +15, Experience +15, Skills≥5 +5"));
        col.addElement(bodyPara("Match Score = Completeness + min(skillCount×2, 20), capped at 100"));
        col.addElement(bodyPara("Dynamic Summary generated from degree + experience + top-5 skills + role"));
        col.addElement(bodyPara("Gap Analysis: expected skills per role vs candidate's verified skills"));
        col.addElement(spacer(4));

        // 11. RECRUITER JSON EXPORT
        col.addElement(sectionHeader("11. RECRUITER JSON EXPORT"));
        col.addElement(bullet("Internal Candidate object NEVER serialized directly"));
        col.addElement(bullet("CandidateReportDto assembled by RecruiterJsonBuilder"));
        col.addElement(bullet("Sections: candidateProfile, contactInformation, technicalSkills, " +
            "education, professionalExperience, projects, certifications, aiRecruiterInsights"));
        col.addElement(bullet("Null values → \"Not Available\" strings"));
        col.addElement(bullet("Empty arrays → descriptive strings (\"No Projects Available\")"));
        col.addElement(bullet("Scores formatted as \"92%\", years as \"3 Years\""));
        col.addElement(bullet("candidateType: \"Fresher\" or \"Experienced\""));
        col.addElement(bullet("aiInsights includes: candidateStrengths, areasForImprovement, " +
            "recommendedRole, candidateReadiness, overallRecommendation, overallAssessment"));
        col.addElement(bullet("Filename: FirstName_LastName_Candidate_Profile.json"));
        col.addElement(spacer(4));

        // 12. EDGE CASES HANDLED
        col.addElement(sectionHeader("12. EDGE CASES HANDLED"));
        col.addElement(bullet("No Skills section → empty skill list (no hallucination)"));
        col.addElement(bullet("Certificate names in resume → not extracted as skills"));
        col.addElement(bullet("Project descriptions → not extracted as skills"));
        col.addElement(bullet("Internship names → not extracted as skills"));
        col.addElement(bullet("Education years → never used for experience calculation"));
        col.addElement(bullet("Fresher with no work dates → yearsExperience = 0, shows \"Fresher\""));
        col.addElement(bullet("Missing CSV fields → graceful fallback to empty string"));
        col.addElement(bullet("Null candidate in session → 400 error with message"));
        col.addElement(bullet("Multi-page PDFs → full text extraction via PDFBox"));
    }

    // ── HELPER: Section header block ──────────────────────────────────────────
    private PdfPTable sectionHeader(String text) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4);
        t.setSpacingAfter(3);

        PdfPCell c = new PdfPCell(new Phrase(text, FONT_SECTION));
        c.setBackgroundColor(COLOR_SECTION_BG);
        c.setBorderColor(COLOR_TABLE_BORDER);
        c.setBorderWidth(0.5f);
        c.setPaddingTop(4);
        c.setPaddingBottom(4);
        c.setPaddingLeft(6);
        c.setPaddingRight(6);
        t.addCell(c);
        return t;
    }

    // ── HELPER: Sub-heading ───────────────────────────────────────────────────
    private Paragraph subHeading(String text) {
        Paragraph p = new Paragraph(text, FONT_BODY_BOLD);
        p.setSpacingBefore(3);
        p.setSpacingAfter(1);
        p.setIndentationLeft(4);
        return p;
    }

    // ── HELPER: Body paragraph ─────────────────────────────────────────────────
    private Paragraph bodyPara(String text) {
        Paragraph p = new Paragraph(text, FONT_BODY);
        p.setSpacingAfter(2);
        p.setIndentationLeft(4);
        return p;
    }

    // ── HELPER: Labeled body ───────────────────────────────────────────────────
    private Paragraph labeledBody(String label, String value) {
        Chunk c1 = new Chunk(label + " ", FONT_BODY_BOLD);
        Chunk c2 = new Chunk(value, FONT_BODY);
        Paragraph p = new Paragraph();
        p.add(c1);
        p.add(c2);
        p.setSpacingAfter(1);
        p.setIndentationLeft(4);
        return p;
    }

    // ── HELPER: Bullet point ──────────────────────────────────────────────────
    private Paragraph bullet(String text) {
        Paragraph p = new Paragraph("  \u2022  " + text, FONT_BULLET);
        p.setSpacingAfter(1);
        p.setIndentationLeft(6);
        return p;
    }

    // ── HELPER: Step paragraph (workflow) ─────────────────────────────────────
    private Paragraph stepPara(String step, String desc) {
        Chunk c1 = new Chunk(step + "  ", FONT_STEP);
        Chunk c2 = new Chunk(desc, FONT_BODY);
        Paragraph p = new Paragraph();
        p.add(c1);
        p.add(c2);
        p.setSpacingAfter(2);
        p.setIndentationLeft(4);
        return p;
    }

    // ── HELPER: Spacer ────────────────────────────────────────────────────────
    private Paragraph spacer(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingAfter(height);
        return p;
    }

    // ── HELPER: Technology stack table ────────────────────────────────────────
    private PdfPTable buildTechTable() throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1f, 2f});
        t.setSpacingBefore(2);
        t.setSpacingAfter(2);

        // Header row
        addTableHeader(t, "Layer");
        addTableHeader(t, "Technology");

        // Data rows
        String[][] rows = {
            {"Backend",       "Java 21, Spring Boot 3"},
            {"PDF Parsing",   "Apache PDFBox 2.0.30"},
            {"CSV Parsing",   "Apache Commons CSV 1.10"},
            {"Frontend",      "Thymeleaf, Bootstrap 5"},
            {"Serialization", "Jackson Databind"},
            {"Build Tool",    "Maven"},
            {"Hosting",       "Embedded Tomcat"}
        };

        boolean alt = false;
        for (String[] row : rows) {
            Color bg = alt ? COLOR_ROW_ALT : COLOR_WHITE;
            addTableCell(t, row[0], bg, true);
            addTableCell(t, row[1], bg, false);
            alt = !alt;
        }
        return t;
    }

    private void addTableHeader(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FONT_TABLE_HDR));
        c.setBackgroundColor(COLOR_ACCENT);
        c.setBorderColor(COLOR_TABLE_BORDER);
        c.setBorderWidth(0.5f);
        c.setPaddingTop(4);
        c.setPaddingBottom(4);
        c.setPaddingLeft(5);
        c.setPaddingRight(5);
        t.addCell(c);
    }

    private void addTableCell(PdfPTable t, String text, Color bg, boolean bold) {
        Font f = bold ? FONT_BODY_BOLD : FONT_TABLE_CELL;
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(bg);
        c.setBorderColor(COLOR_TABLE_BORDER);
        c.setBorderWidth(0.5f);
        c.setPaddingTop(3);
        c.setPaddingBottom(3);
        c.setPaddingLeft(5);
        c.setPaddingRight(5);
        t.addCell(c);
    }

    // ── PAGE FOOTER EVENT ─────────────────────────────────────────────────────
    static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float pageWidth  = document.getPageSize().getWidth();
            float bottom     = document.bottomMargin();

            // Footer separator line
            cb.setColorStroke(new Color(229, 231, 235));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), bottom + 18);
            cb.lineTo(pageWidth - document.rightMargin(), bottom + 18);
            cb.stroke();

            // Left label
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Candidate Transformer AI — Design Document", new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(107, 114, 128))),
                document.leftMargin(), bottom + 8, 0);

            // Right page number
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Page " + writer.getPageNumber(), new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(107, 114, 128))),
                pageWidth - document.rightMargin(), bottom + 8, 0);
        }
    }
}
