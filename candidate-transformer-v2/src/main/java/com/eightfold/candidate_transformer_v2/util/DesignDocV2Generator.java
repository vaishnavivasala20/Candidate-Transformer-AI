package com.eightfold.candidate_transformer_v2.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates the professional design document PDF + architecture/workflow diagrams.
 * Run: mvnw exec:java -Dexec.mainClass="com.eightfold.candidate_transformer_v2.util.DesignDocV2Generator"
 */
public class DesignDocV2Generator {

    // ── Base path ──────────────────────────────────────────────────────────────
    private static final String BASE =
        "C:/Users/HP/Downloads/candidate-transformer-v2/candidate-transformer-v2/";

    // ── Palette — white bg, near-black text, one blue accent, grays ───────────
    private static final Color BLUE       = new Color(37,  99,  235);
    private static final Color BLUE_LIGHT = new Color(219, 234, 254);
    private static final Color TEXT       = new Color(17,  24,  39);
    private static final Color GRAY_DARK  = new Color(55,  65,  81);
    private static final Color GRAY_MID   = new Color(107, 114, 128);
    private static final Color GRAY_RULE  = new Color(209, 213, 219);
    private static final Color WHITE      = Color.WHITE;
    private static final Color ROW_ALT    = new Color(249, 250, 251);

    // ── Fonts ──────────────────────────────────────────────────────────────────
    private static final Font F_DOC_TITLE  = new Font(Font.HELVETICA, 18, Font.BOLD,   TEXT);
    private static final Font F_DOC_SUB    = new Font(Font.HELVETICA,  9, Font.NORMAL, GRAY_MID);
    private static final Font F_SEC_HEAD   = new Font(Font.HELVETICA,  8, Font.BOLD,   BLUE);
    private static final Font F_BODY       = new Font(Font.HELVETICA,  8, Font.NORMAL, TEXT);
    private static final Font F_BODY_BOLD  = new Font(Font.HELVETICA,  8, Font.BOLD,   TEXT);
    private static final Font F_BODY_GRAY  = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_DARK);
    private static final Font F_SMALL      = new Font(Font.HELVETICA,  7, Font.NORMAL, GRAY_MID);
    private static final Font F_TBL_HDR    = new Font(Font.HELVETICA,  8, Font.BOLD,   WHITE);
    private static final Font F_TBL_CELL   = new Font(Font.HELVETICA,  8, Font.NORMAL, TEXT);
    private static final Font F_TBL_CELL_B = new Font(Font.HELVETICA,  8, Font.BOLD,   GRAY_DARK);
    private static final Font F_DIAG_TITLE = new Font(Font.HELVETICA, 13, Font.BOLD,   TEXT);
    private static final Font F_DIAG_BOX   = new Font(Font.HELVETICA,  9, Font.BOLD,   TEXT);
    private static final Font F_DIAG_SUB   = new Font(Font.HELVETICA,  8, Font.NORMAL, GRAY_DARK);
    private static final Font F_DIAG_ARROW = new Font(Font.HELVETICA, 11, Font.NORMAL, GRAY_MID);
    private static final Font F_STEP_NUM   = new Font(Font.HELVETICA,  8, Font.BOLD,   BLUE);

    public static void main(String[] args) throws Exception {
        DesignDocV2Generator g = new DesignDocV2Generator();
        g.generateMainDocument();
        g.generateArchDiagram();
        g.generateWorkflowDiagram();
        System.out.println("All files generated in: " + BASE);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // MAIN DOCUMENT
    // ══════════════════════════════════════════════════════════════════════════
    public void generateMainDocument() throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 48, 48, 44, 44);
        PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream(BASE + "Design_Document.pdf"));
        w.setPageEvent(new PageFooter());
        doc.open();

        // ── Document header ───────────────────────────────────────────────────
        Paragraph title = new Paragraph("Candidate Transformer AI", F_DOC_TITLE);
        title.setSpacingAfter(3);
        doc.add(title);

        Paragraph sub = new Paragraph(
            "Design Document  ·  Spring Boot 3  ·  Java 21  ·  Apache PDFBox  ·  Thymeleaf", F_DOC_SUB);
        sub.setSpacingAfter(6);
        doc.add(sub);

        // blue rule
        doc.add(rule(BLUE, 1.5f));
        doc.add(space(6));

        // ── Two-column layout ─────────────────────────────────────────────────
        PdfPTable layout = new PdfPTable(2);
        layout.setWidthPercentage(100);
        layout.setWidths(new float[]{1f, 1f});
        PdfPCell left  = nopad(); left.setPaddingRight(12);
        PdfPCell right = nopad(); right.setPaddingLeft(12);
        right.setBorderWidthLeft(0.5f);
        right.setBorderColorLeft(GRAY_RULE);

        buildLeft(left);
        buildRight(right);

        layout.addCell(left);
        layout.addCell(right);
        doc.add(layout);

        doc.close();
        System.out.println("  ✓ Design_Document.pdf");
    }

    // ── LEFT column ───────────────────────────────────────────────────────────
    private void buildLeft(PdfPCell c) throws DocumentException {

        // 1. PROJECT OVERVIEW
        c.addElement(secHead("1.  Project Overview"));
        c.addElement(body(
            "Candidate Transformer AI is an ATS-style recruiter tool that eliminates " +
            "manual resume screening. Recruiters upload a structured CSV containing " +
            "candidate metadata and an unstructured Resume PDF. The application " +
            "automatically parses, normalises, merges, and AI-analyses both sources — " +
            "generating a unified candidate profile and a recruiter-friendly JSON report " +
            "suitable for sharing with hiring managers."));
        c.addElement(space(8));

        // 2. TECHNOLOGY STACK
        c.addElement(secHead("2.  Technology Stack"));
        c.addElement(buildTechTable());
        c.addElement(space(8));

        // 4. END-TO-END WORKFLOW
        c.addElement(secHead("4.  End-to-End Workflow"));
        String[][] steps = {
            {"1", "Upload",    "Recruiter submits CSV + Resume PDF via web form"},
            {"2", "Parse",     "CsvParser maps CSV headers; ResumeParser segments PDF sections"},
            {"3", "Normalise", "Names title-cased; emails lowercased; phones standardised (+91)"},
            {"4", "Merge",     "CSV metadata and resume content unified into one Candidate object"},
            {"5", "Analyse",   "AIAnalyzer scores skills across 9 role clusters; computes match score"},
            {"6", "Profile",   "Thymeleaf renders recruiter-friendly candidate report in browser"},
            {"7", "Export",    "DownloadController serves JSON via CandidateReportDto (DTO pattern)"},
        };
        for (String[] s : steps) c.addElement(step(s[0], s[1], s[2]));
        c.addElement(space(8));

        // 6. EDGE CASES HANDLED
        c.addElement(secHead("6.  Edge Cases Handled"));
        String[] edges = {
            "Fresher resume with no work experience → yearsExperience = 0, labelled \"Fresher\"",
            "Resume with no Skills section → empty skill list returned, no guessing",
            "Certificate names adjacent to Skills section → stop condition prevents extraction",
            "Education dates → never used for experience calculation (dedicated section check)",
            "Missing email or phone → empty list, not null; no crash in view layer",
            "Duplicate skills → deduplicated alphabetically before display",
            "Missing CSV field → graceful fallback returns empty string via getValue()",
            "No candidate in session → HTTP 400 returned with readable error message",
        };
        for (String e : edges) c.addElement(bullet(e));
    }

    // ── RIGHT column ──────────────────────────────────────────────────────────
    private void buildRight(PdfPCell c) throws DocumentException {

        // 3. SYSTEM ARCHITECTURE  (inline miniature diagram in right column)
        c.addElement(secHead("3.  System Architecture"));
        c.addElement(buildArchMini());
        c.addElement(space(8));

        // 5. DESIGN DECISIONS
        c.addElement(secHead("5.  Design Decisions"));
        Object[][] decisions = {
            {"Section-based PDF parsing",
             "Segments the resume into named sections before extraction, preventing " +
             "content from one section leaking into another."},
            {"Strict whitelist skill extraction",
             "Skills are matched only against a curated ~120-entry technology list, " +
             "eliminating certificate names and project descriptions from skill output."},
            {"Separation of parsing, normalisation, and merging",
             "Each responsibility is a dedicated class (CsvParser, ResumeParser, " +
             "Normalizer, Merger), making the pipeline independently testable."},
            {"Rule-based AI analyser (no external API)",
             "Role detection, match scoring, and gap analysis run entirely in-process, " +
             "ensuring zero latency, offline operation, and no API cost."},
            {"DTO pattern for JSON export",
             "The internal Candidate model is never serialised directly; RecruiterJsonBuilder " +
             "assembles a clean CandidateReportDto, keeping recruiter output decoupled from " +
             "implementation details."},
            {"Resume always preferred over CSV for structured data",
             "Education, experience, projects, and certifications are always taken from the " +
             "resume; CSV contributes metadata (headline, location) only."},
        };
        for (Object[] d : decisions) {
            c.addElement(decisionItem((String)d[0], (String)d[1]));
        }
        c.addElement(space(8));

        // 7. FUTURE IMPROVEMENTS
        c.addElement(secHead("7.  Future Improvements"));
        String[] future = {
            "Job Description Matching — score each candidate against a specific JD for ranked fit.",
            "LLM-powered summary — replace rule-based summary with GPT-4 generated narrative.",
            "Batch resume processing — allow multiple PDF uploads in a single session.",
        };
        for (String f : future) c.addElement(bullet(f));
    }

    // ── Tech table ────────────────────────────────────────────────────────────
    private PdfPTable buildTechTable() throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.1f, 1.9f});
        t.setSpacingBefore(2); t.setSpacingAfter(2);

        th(t, "Layer"); th(t, "Technology");

        String[][] rows = {
            {"Backend",        "Java 21, Spring Boot 3, Spring MVC"},
            {"Frontend",       "Thymeleaf, Bootstrap 5, HTML/CSS"},
            {"PDF Parsing",    "Apache PDFBox 2.0.30"},
            {"CSV Parsing",    "Apache Commons CSV 1.10.0"},
            {"JSON",           "Jackson Databind"},
            {"Build Tool",     "Maven (Spring Boot Maven Plugin)"},
            {"Server",         "Embedded Apache Tomcat"},
        };
        boolean alt = false;
        for (String[] r : rows) { td(t, r[0], alt, true); td(t, r[1], alt, false); alt = !alt; }
        return t;
    }

    // ── Inline architecture mini-diagram ──────────────────────────────────────
    private PdfPTable buildArchMini() throws DocumentException {
        // Single-column table of boxes and arrows
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(2); t.setSpacingAfter(2);

        String[][] nodes = {
            // {label, sublabel, type}  type: "actor","ctrl","svc","util","out"
            {"Recruiter",                     "",                        "actor"},
            {"↓",                             "",                        "arrow"},
            {"Upload CSV  +  Resume PDF",     "Web Form (upload.html)",  "ctrl"},
            {"↓",                             "",                        "arrow"},
            {"Transform Controller",          "POST /transform",         "ctrl"},
            {"↓",                             "",                        "arrow"},
            {"Transform Service",             "Orchestration layer",     "svc"},
            {"↓",                             "",                        "arrow"},
            {"CSV Parser  ·  Resume Parser",  "Section-based PDF parse", "util"},
            {"↓",                             "",                        "arrow"},
            {"Normalizer  ·  Merger",         "Canonical data + merge",  "util"},
            {"↓",                             "",                        "arrow"},
            {"AI Analyzer",                   "Role · Score · Gaps",     "svc"},
            {"↓",                             "",                        "arrow"},
            {"Candidate Profile",             "index.html (Thymeleaf)",  "out"},
            {"↓",                             "",                        "arrow"},
            {"JSON Export",                   "GET /download/json",      "out"},
        };

        for (String[] n : nodes) {
            if (n[2].equals("arrow")) {
                PdfPCell ac = new PdfPCell(new Phrase(n[0], F_DIAG_ARROW));
                ac.setBorder(Rectangle.NO_BORDER);
                ac.setHorizontalAlignment(Element.ALIGN_CENTER);
                ac.setPaddingTop(1); ac.setPaddingBottom(1);
                t.addCell(ac);
            } else {
                Color bg;
                Font labelFont;
                switch (n[2]) {
                    case "actor": bg = BLUE;       labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, WHITE); break;
                    case "ctrl":  bg = new Color(243,244,246); labelFont = F_DIAG_BOX; break;
                    case "svc":   bg = new Color(239,246,255); labelFont = F_DIAG_BOX; break;
                    case "util":  bg = new Color(249,250,251); labelFont = F_DIAG_BOX; break;
                    default:      bg = new Color(236,253,245); labelFont = F_DIAG_BOX; break;
                }
                PdfPCell cell = new PdfPCell();
                cell.setBackgroundColor(bg);
                if (n[2].equals("actor")) {
                    cell.setBorderColor(BLUE);
                } else {
                    cell.setBorderColor(GRAY_RULE);
                }
                cell.setBorderWidth(0.5f);
                cell.setPaddingTop(4); cell.setPaddingBottom(4);
                cell.setPaddingLeft(8); cell.setPaddingRight(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);

                Paragraph p = new Paragraph(n[0], labelFont);
                p.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(p);
                if (!n[1].isEmpty()) {
                    Paragraph sub = new Paragraph(n[1], F_SMALL);
                    sub.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(sub);
                }
                t.addCell(cell);
            }
        }
        return t;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STANDALONE ARCHITECTURE DIAGRAM (hi-res A3 landscape)
    // ══════════════════════════════════════════════════════════════════════════
    public void generateArchDiagram() throws DocumentException, IOException {
        Rectangle page = new Rectangle(PageSize.A3.rotate()); // landscape
        Document doc = new Document(page, 60, 60, 50, 50);
        PdfWriter w = PdfWriter.getInstance(doc,
            new FileOutputStream(BASE + "Architecture_Diagram.pdf"));
        doc.open();
        PdfContentByte cb = w.getDirectContent();
        float pw = page.getWidth(), ph = page.getHeight();

        // Title
        drawText(cb, "Candidate Transformer AI — System Architecture",
                 pw/2, ph - 55, 16, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Spring Boot 3  ·  Java 21  ·  Section-Based PDF Parsing  ·  Rule-Based AI Analyzer",
                 pw/2, ph - 75, 9, Font.NORMAL, GRAY_MID, Element.ALIGN_CENTER);

        // Horizontal rule
        cb.setColorStroke(GRAY_RULE); cb.setLineWidth(0.7f);
        cb.moveTo(60, ph - 85); cb.lineTo(pw - 60, ph - 85); cb.stroke();

        // ── Row 1: Input layer ────────────────────────────────────────────────
        float rowY = ph - 150;
        float bw = 140, bh = 48;

        // Actor box
        drawBox(cb, 80, rowY, 100, 48, BLUE, BLUE, 1f);
        drawText(cb, "Recruiter", 130, rowY + 30, 11, Font.BOLD, WHITE, Element.ALIGN_CENTER);
        drawText(cb, "Uploads two files", 130, rowY + 16, 8, Font.NORMAL, BLUE_LIGHT, Element.ALIGN_CENTER);

        // Arrow
        drawArrow(cb, 180, rowY + 24, 230, rowY + 24);

        // Upload form
        drawBox(cb, 230, rowY, 150, 48, new Color(243,244,246), GRAY_RULE, 0.5f);
        drawText(cb, "Web Upload Form", 305, rowY + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "CSV  +  Resume PDF", 305, rowY + 16, 8, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // Arrow
        drawArrow(cb, 380, rowY + 24, 430, rowY + 24);

        // Transform Controller
        drawBox(cb, 430, rowY, 160, 48, new Color(239,246,255), BLUE, 0.8f);
        drawText(cb, "Transform Controller", 510, rowY + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "POST /transform", 510, rowY + 16, 8, Font.NORMAL, GRAY_MID, Element.ALIGN_CENTER);

        // Arrow
        drawArrow(cb, 590, rowY + 24, 640, rowY + 24);

        // Transform Service
        drawBox(cb, 640, rowY, 155, 48, new Color(239,246,255), BLUE, 0.8f);
        drawText(cb, "Transform Service", 717, rowY + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Orchestration layer", 717, rowY + 16, 8, Font.NORMAL, GRAY_MID, Element.ALIGN_CENTER);

        // ── Row 2: Parser layer ───────────────────────────────────────────────
        float row2Y = rowY - 110;

        // Down arrow from Transform Service
        drawArrow(cb, 717, rowY, 717, row2Y + 48);

        // Horizontal line to both parsers
        cb.setColorStroke(GRAY_MID); cb.setLineWidth(1f);
        cb.moveTo(565, row2Y + 72); cb.lineTo(870, row2Y + 72); cb.stroke();
        drawArrow(cb, 565, row2Y + 72, 565, row2Y + 48);
        drawArrow(cb, 870, row2Y + 72, 870, row2Y + 48);

        // CSV Parser
        drawBox(cb, 490, row2Y, 150, 48, new Color(249,250,251), GRAY_RULE, 0.5f);
        drawText(cb, "CSV Parser", 565, row2Y + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Commons CSV · header-aware", 565, row2Y + 16, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // Resume Parser
        drawBox(cb, 795, row2Y, 150, 48, new Color(249,250,251), GRAY_RULE, 0.5f);
        drawText(cb, "Resume Parser", 870, row2Y + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "PDFBox · section-based", 870, row2Y + 16, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // ── Row 3: Normalizer + Merger ────────────────────────────────────────
        float row3Y = row2Y - 110;

        drawArrow(cb, 565, row2Y, 565, row3Y + 48);
        drawArrow(cb, 870, row3Y + 72, 870, row2Y);

        // Horizontal connector for both to Normalizer
        cb.setColorStroke(GRAY_MID); cb.setLineWidth(1f);
        cb.moveTo(565, row3Y + 72); cb.lineTo(870, row3Y + 72); cb.stroke();
        float midX = (565 + 870) / 2f;
        drawArrow(cb, midX, row3Y + 72, midX, row3Y + 48);

        // Normalizer
        drawBox(cb, midX - 80, row3Y, 160, 48, new Color(249,250,251), GRAY_RULE, 0.5f);
        drawText(cb, "Normalizer", midX, row3Y + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Name · Email · Phone · Skills", midX, row3Y + 16, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // Arrow to Merger
        float mergeX = midX + 220;
        drawArrow(cb, midX + 80, row3Y + 24, mergeX - 80, row3Y + 24);

        // Merger
        drawBox(cb, mergeX - 80, row3Y, 160, 48, new Color(249,250,251), GRAY_RULE, 0.5f);
        drawText(cb, "Merger", mergeX, row3Y + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "CSV + Resume → Candidate", mergeX, row3Y + 16, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // ── Row 4: AI + Output ────────────────────────────────────────────────
        float row4Y = row3Y - 110;

        drawArrow(cb, mergeX, row3Y, mergeX, row4Y + 48);

        // AI Analyzer
        drawBox(cb, mergeX - 80, row4Y, 160, 48, new Color(239,246,255), BLUE, 0.8f);
        drawText(cb, "AI Analyzer", mergeX, row4Y + 30, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Role · Score · Gaps · Summary", mergeX, row4Y + 16, 7, Font.NORMAL, GRAY_MID, Element.ALIGN_CENTER);

        // Two outputs
        float outLeft  = mergeX - 160, outRight = mergeX + 160;
        cb.setColorStroke(GRAY_MID); cb.setLineWidth(1f);
        cb.moveTo(outLeft, row4Y - 30); cb.lineTo(outRight, row4Y - 30); cb.stroke();
        drawArrow(cb, outLeft, row4Y - 30, outLeft, row4Y - 80);
        drawArrow(cb, outRight, row4Y - 30, outRight, row4Y - 80);
        cb.moveTo(mergeX, row4Y); cb.lineTo(mergeX, row4Y - 30); cb.stroke();

        // Candidate Profile output
        drawBox(cb, outLeft - 80, row4Y - 128, 160, 48, new Color(236,253,245), new Color(134,239,172), 0.5f);
        drawText(cb, "Candidate Profile", outLeft, row4Y - 112+14, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Thymeleaf · index.html", outLeft, row4Y - 112, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // JSON Export output
        drawBox(cb, outRight - 80, row4Y - 128, 160, 48, new Color(236,253,245), new Color(134,239,172), 0.5f);
        drawText(cb, "JSON Export", outRight, row4Y - 112+14, 9, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "GET /download/json", outRight, row4Y - 112, 7, Font.NORMAL, GRAY_DARK, Element.ALIGN_CENTER);

        // ── Legend ────────────────────────────────────────────────────────────
        float lx = 60, ly = 80;
        drawText(cb, "Legend:", lx, ly + 18, 8, Font.BOLD, GRAY_DARK, Element.ALIGN_LEFT);
        drawBox(cb, lx + 55, ly, 14, 14, BLUE, BLUE, 1f);
        drawText(cb, "Actor / User", lx + 75, ly + 10, 7, Font.NORMAL, GRAY_MID, Element.ALIGN_LEFT);
        drawBox(cb, lx + 150, ly, 14, 14, new Color(239,246,255), BLUE, 0.8f);
        drawText(cb, "Controller / Service", lx + 170, ly + 10, 7, Font.NORMAL, GRAY_MID, Element.ALIGN_LEFT);
        drawBox(cb, lx + 270, ly, 14, 14, new Color(249,250,251), GRAY_RULE, 0.5f);
        drawText(cb, "Utility / Parser", lx + 290, ly + 10, 7, Font.NORMAL, GRAY_MID, Element.ALIGN_LEFT);
        drawBox(cb, lx + 380, ly, 14, 14, new Color(236,253,245), new Color(134,239,172), 0.5f);
        drawText(cb, "Output", lx + 400, ly + 10, 7, Font.NORMAL, GRAY_MID, Element.ALIGN_LEFT);

        doc.close();
        System.out.println("  ✓ Architecture_Diagram.pdf");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STANDALONE WORKFLOW DIAGRAM (A4 portrait)
    // ══════════════════════════════════════════════════════════════════════════
    public void generateWorkflowDiagram() throws DocumentException, IOException {
        Document doc = new Document(PageSize.A4, 60, 60, 50, 50);
        PdfWriter w = PdfWriter.getInstance(doc,
            new FileOutputStream(BASE + "Workflow_Diagram.pdf"));
        doc.open();
        PdfContentByte cb = w.getDirectContent();
        float pw = PageSize.A4.getWidth(), ph = PageSize.A4.getHeight();

        // Title
        drawText(cb, "Candidate Transformer AI — End-to-End Workflow",
                 pw/2, ph - 55, 15, Font.BOLD, TEXT, Element.ALIGN_CENTER);
        drawText(cb, "Seven-step pipeline from file upload to JSON export",
                 pw/2, ph - 73, 9, Font.NORMAL, GRAY_MID, Element.ALIGN_CENTER);
        cb.setColorStroke(GRAY_RULE); cb.setLineWidth(0.7f);
        cb.moveTo(60, ph - 82); cb.lineTo(pw - 60, ph - 82); cb.stroke();

        // Steps
        String[][] wfSteps = {
            {"1", "Upload",
             "Recruiter visits / and submits two files via POST /transform",
             "Multipart form · CSV + Resume PDF"},
            {"2", "Parse",
             "CsvParser reads header-mapped rows. ResumeParser segments the PDF into named sections " +
             "(Skills, Experience, Education, Projects, Certifications) using heading detection.",
             "Apache Commons CSV · Apache PDFBox"},
            {"3", "Normalise",
             "Normalizer standardises all field values: names to Title Case, emails to lowercase, " +
             "10-digit phones prefixed +91, and skill aliases resolved to canonical names.",
             "Normalizer.java"},
            {"4", "Merge",
             "Merger combines CSV metadata (headline, location) with resume-sourced content " +
             "(skills, education, experience). Resume data always takes precedence.",
             "Merger.java · Field priority rules"},
            {"5", "AI Analyse",
             "AIAnalyzer scores candidate skills against 9 role clusters, computes a match score " +
             "(0–100), identifies skill gaps, and generates a dynamic professional summary.",
             "AIAnalyzer.java · Rule-based engine"},
            {"6", "Generate Profile",
             "TransformController stores the Candidate in the HTTP session and renders the " +
             "recruiter-friendly profile page via Thymeleaf.",
             "index.html · Bootstrap 5"},
            {"7", "Export JSON",
             "DownloadController assembles a CandidateReportDto via RecruiterJsonBuilder and " +
             "serves a pretty-printed, recruiter-readable JSON file with a personalised filename.",
             "GET /download/json · Jackson"},
        };

        float cx = pw / 2;
        float startY = ph - 115;
        float stepH = 68, stepW = 390;

        // Draw vertical spine
        float spineX = cx;
        float spineTop = startY;
        float spineBot = startY - (wfSteps.length - 1) * stepH - 40;
        cb.setColorStroke(BLUE_LIGHT); cb.setLineWidth(2f);
        cb.moveTo(spineX, spineTop); cb.lineTo(spineX, spineBot); cb.stroke();

        for (int i = 0; i < wfSteps.length; i++) {
            float yc = startY - i * stepH;
            String[] s = wfSteps[i];

            // Circle on spine
            cb.setColorFill(BLUE); cb.setColorStroke(BLUE); cb.setLineWidth(1f);
            cb.circle(spineX, yc, 12);
            cb.fillStroke();
            drawText(cb, s[0], spineX, yc - 4, 9, Font.BOLD, WHITE, Element.ALIGN_CENTER);

            // Box — alternating left/right
            boolean left = (i % 2 == 0);
            float bx = left ? spineX - stepW - 20 : spineX + 20;
            float by = yc - 28;
            drawBox(cb, bx, by, stepW, 52, new Color(249,250,251), GRAY_RULE, 0.5f);

            // Step name
            drawText(cb, s[1], bx + (left ? stepW - 8 : 8), by + 40,
                     10, Font.BOLD, TEXT,
                     left ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);

            // Description — manual wrap simulation via short phrases
            drawText(cb, s[2], bx + (left ? stepW - 8 : 8), by + 27,
                     7, Font.NORMAL, GRAY_DARK,
                     left ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);

            // Tech tag
            drawText(cb, s[3], bx + (left ? stepW - 8 : 8), by + 10,
                     7, Font.ITALIC, BLUE,
                     left ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT);

            // Connector line from circle to box
            float lineX = left ? spineX - 12 : spineX + 12;
            float boxEdgeX = left ? bx + stepW : bx;
            cb.setColorStroke(BLUE_LIGHT); cb.setLineWidth(1f);
            cb.moveTo(lineX, yc); cb.lineTo(boxEdgeX, yc); cb.stroke();
        }

        // Footer note
        drawText(cb, "All steps execute within a single HTTP request · No external API calls · In-process only",
                 pw/2, 45, 7, Font.ITALIC, GRAY_MID, Element.ALIGN_CENTER);

        doc.close();
        System.out.println("  ✓ Workflow_Diagram.pdf");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // HELPERS — Table cells
    // ══════════════════════════════════════════════════════════════════════════
    private void th(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TBL_HDR));
        c.setBackgroundColor(BLUE);
        c.setBorderColor(BLUE); c.setBorderWidth(0.5f);
        c.setPaddingTop(4); c.setPaddingBottom(4);
        c.setPaddingLeft(6); c.setPaddingRight(6);
        t.addCell(c);
    }
    private void td(PdfPTable t, String text, boolean alt, boolean bold) {
        Font f = bold ? F_TBL_CELL_B : F_TBL_CELL;
        PdfPCell c = new PdfPCell(new Phrase(text, f));
        c.setBackgroundColor(alt ? ROW_ALT : WHITE);
        c.setBorderColor(GRAY_RULE); c.setBorderWidth(0.5f);
        c.setPaddingTop(3); c.setPaddingBottom(3);
        c.setPaddingLeft(6); c.setPaddingRight(6);
        t.addCell(c);
    }

    // ── Document helpers ──────────────────────────────────────────────────────
    private PdfPTable secHead(String text) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        t.setSpacingBefore(6); t.setSpacingAfter(4);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColorBottom(BLUE); c.setBorderWidthBottom(0.8f);
        c.setPaddingBottom(3); c.setPaddingLeft(0);
        Chunk dot = new Chunk("\u25A0 ", new Font(Font.HELVETICA, 7, Font.NORMAL, BLUE));
        Chunk lbl = new Chunk(text, F_SEC_HEAD);
        Paragraph p = new Paragraph(); p.add(dot); p.add(lbl);
        c.addElement(p);
        t.addCell(c);
        return t;
    }
    private Paragraph body(String text) {
        Paragraph p = new Paragraph(text, F_BODY_GRAY);
        p.setSpacingAfter(2); p.setLeading(12);
        return p;
    }
    private Paragraph bullet(String text) {
        Paragraph p = new Paragraph("  \u2022  " + text, F_BODY);
        p.setSpacingAfter(2); p.setIndentationLeft(4); p.setLeading(11);
        return p;
    }
    private Paragraph step(String num, String name, String desc) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(num + ". ", F_STEP_NUM));
        p.add(new Chunk(name + "  \u2014  ", F_BODY_BOLD));
        p.add(new Chunk(desc, F_BODY));
        p.setSpacingAfter(3); p.setLeading(11);
        return p;
    }
    private Paragraph decisionItem(String title, String desc) {
        Paragraph p = new Paragraph();
        p.add(new Chunk("\u25B6  " + title + "\n", F_BODY_BOLD));
        p.add(new Chunk("    " + desc, F_BODY_GRAY));
        p.setSpacingAfter(5); p.setLeading(11);
        return p;
    }
    private PdfPTable rule(Color c, float w) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBackgroundColor(c); cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(w);
        t.addCell(cell);
        return t;
    }
    private Paragraph space(float h) {
        Paragraph p = new Paragraph(" "); p.setSpacingAfter(h); return p;
    }
    private PdfPCell nopad() {
        PdfPCell c = new PdfPCell(); c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0); return c;
    }

    // ── Canvas drawing helpers ─────────────────────────────────────────────────
    private void drawBox(PdfContentByte cb, float x, float y, float w, float h,
                         Color fill, Color stroke, float sw) {
        cb.setColorFill(fill); cb.setColorStroke(stroke); cb.setLineWidth(sw);
        cb.roundRectangle(x, y, w, h, 4);
        cb.fillStroke();
    }
    private void drawText(PdfContentByte cb, String text, float x, float y,
                          int size, int style, Color color, int align) {
        cb.beginText();
        cb.setFontAndSize(style == Font.BOLD ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, size, color).getBaseFont()
                : style == Font.ITALIC ? FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, size, color).getBaseFont()
                : FontFactory.getFont(FontFactory.HELVETICA, size, color).getBaseFont(), size);
        cb.setColorFill(color);
        float offset = align == Element.ALIGN_CENTER ? 0 : align == Element.ALIGN_RIGHT ? -1 : 1;
        if (align == Element.ALIGN_CENTER)      cb.showTextAligned(Element.ALIGN_CENTER, text, x, y, 0);
        else if (align == Element.ALIGN_RIGHT)  cb.showTextAligned(Element.ALIGN_RIGHT,  text, x, y, 0);
        else                                    cb.showTextAligned(Element.ALIGN_LEFT,   text, x, y, 0);
        cb.endText();
    }
    private void drawArrow(PdfContentByte cb, float x1, float y1, float x2, float y2) {
        cb.setColorStroke(GRAY_MID); cb.setColorFill(GRAY_MID); cb.setLineWidth(1f);
        cb.moveTo(x1, y1); cb.lineTo(x2, y2); cb.stroke();
        // arrowhead
        double angle = Math.atan2(y2 - y1, x2 - x1);
        float al = 7f;
        float ax1 = (float)(x2 - al * Math.cos(angle - 0.4));
        float ay1 = (float)(y2 - al * Math.sin(angle - 0.4));
        float ax2 = (float)(x2 - al * Math.cos(angle + 0.4));
        float ay2 = (float)(y2 - al * Math.sin(angle + 0.4));
        cb.moveTo(x2, y2); cb.lineTo(ax1, ay1); cb.lineTo(ax2, ay2); cb.closePath(); cb.fill();
    }

    // ── Page footer ───────────────────────────────────────────────────────────
    static class PageFooter extends PdfPageEventHelper {
        @Override public void onEndPage(PdfWriter w, Document d) {
            PdfContentByte cb = w.getDirectContent();
            float pw = d.getPageSize().getWidth();
            float bot = d.bottomMargin();
            cb.setColorStroke(new Color(229,231,235)); cb.setLineWidth(0.5f);
            cb.moveTo(d.leftMargin(), bot + 16);
            cb.lineTo(pw - d.rightMargin(), bot + 16); cb.stroke();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Candidate Transformer AI  ·  Design Document",
                    new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(156,163,175))),
                d.leftMargin(), bot + 6, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("Page " + w.getPageNumber(),
                    new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(156,163,175))),
                pw - d.rightMargin(), bot + 6, 0);
        }
    }
}
