package com.eightfold.candidate_transformer_v2.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.*;

/**
 * Premium one-page landscape design document.
 * A4 landscape, strictly one page, 2-column layout with canvas-drawn
 * architecture diagram, horizontal workflow pipeline, and screen mockups.
 *
 * Audit fixes applied:
 *  - Removed "under two seconds" and "bias-free" (not in code)
 *  - Correct claim: 9 skill-cluster scoring (verified in AIAnalyzer.java)
 *  - Architecture shows grouped Spring Boot backend container
 *  - Workflow as horizontal numbered tiles
 *  - Screen mockups drawn accurately from upload.html / index.html
 *  - No tables for layout (PdfPTable cells used only for grid, content uses canvas)
 */
public class PremiumDoc {

    private static final String OUT =
        "C:/Users/HP/Downloads/candidate-transformer-v2/candidate-transformer-v2/";
    private static final String FILENAME = "Candidate_Transformer_AI_Design_Document.pdf";

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color NAVY      = new Color(10,  15,  38);
    private static final Color DARK      = new Color(28,  37,  60);
    private static final Color SLATE     = new Color(55,  65,  90);
    private static final Color MID       = new Color(95, 110, 135);
    private static final Color LIGHT     = new Color(148, 163, 185);
    private static final Color HAIRLINE  = new Color(220, 227, 237);
    private static final Color BG_CARD   = new Color(250, 251, 253);
    private static final Color BG_TAG    = new Color(242, 246, 255);
    private static final Color WHITE     = Color.WHITE;
    private static final Color ACCENT    = new Color(41,  98,  225);
    private static final Color ACCENT_S  = new Color(210, 224, 252);
    private static final Color GREEN     = new Color(22, 163,  74);
    private static final Color GRN_BG    = new Color(240, 253, 244);
    private static final Color GRN_BD    = new Color(187, 247, 208);
    private static final Color AMBER     = new Color(180, 120,  20);
    private static final Color AMB_BG    = new Color(255, 251, 235);

    // ── Dimensions ────────────────────────────────────────────────────────────
    // A4 landscape = 841.89 × 595.28 pt ≈ 842 × 595
    private static final float PW = 841.89f;
    private static final float PH = 595.28f;
    private static final float ML = 30f, MR = 30f, MT = 28f, MB = 26f;
    // Usable: ~782 × 541 pt
    private static final float UW = PW - ML - MR;  // ~782
    private static final float COL_GAP = 10f;
    private static final float LC = (UW - COL_GAP) * 0.44f;  // left col ~339
    private static final float RC = (UW - COL_GAP) * 0.56f;  // right col ~432

    // ── Base fonts ────────────────────────────────────────────────────────────
    private BaseFont HB, HN, HI;

    public static void main(String[] args) throws Exception {
        new PremiumDoc().run();
    }

    public void run() throws Exception {
        HB = BaseFont.createFont(BaseFont.HELVETICA_BOLD,    BaseFont.CP1252, false);
        HN = BaseFont.createFont(BaseFont.HELVETICA,         BaseFont.CP1252, false);
        HI = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, false);

        generate();
        System.out.println("Generated: " + OUT + FILENAME);
    }

    private void generate() throws Exception {
        Rectangle page = new Rectangle(PW, PH);
        Document doc = new Document(page, ML, MR, MT, MB);
        PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream(OUT + FILENAME));
        doc.open();

        PdfContentByte cb = w.getDirectContent();

        // All drawing is done on the canvas — full control, no overflow
        drawDocument(cb);

        doc.close();
    }

    private void drawDocument(PdfContentByte cb) throws Exception {

        // ── HEADER (top band) ─────────────────────────────────────────────────
        float hdrBottom = PH - MT - 46f;
        drawHeader(cb, hdrBottom);

        // Thin accent line under header
        hline(cb, ML, PW - MR, hdrBottom - 2, ACCENT, 1.0f);

        // ── COLUMN TOPS ───────────────────────────────────────────────────────
        float bodyTop    = hdrBottom - 9f;
        float bodyBottom = MB + 52f;  // leave room for footer + screenshots
        float leftX  = ML;
        float rightX = ML + LC + COL_GAP;

        // Left column
        float y = drawLeftColumn(cb, leftX, bodyTop, bodyBottom);

        // Right column
        drawRightColumn(cb, rightX, bodyTop, bodyBottom);

        // ── SCREENSHOT MOCKUPS (bottom strip) ─────────────────────────────────
        float ssTop    = MB + 48f;
        float ssBottom = MB + 10f;
        drawScreenshots(cb, ML, rightX + RC, ssTop, ssBottom);

        // ── FOOTER ────────────────────────────────────────────────────────────
        drawFooter(cb);
    }

    // =========================================================================
    // HEADER
    // =========================================================================
    private void drawHeader(PdfContentByte cb, float bottom) throws Exception {
        float top = PH - MT;

        // Title
        txt(cb, HB, 19, NAVY, ML, top - 15, "Candidate Transformer AI");

        // Subtitle on same line far right
        txt(cb, HN, 7.5f, MID, ML, top - 24,
            "AI-Powered Candidate Intelligence Platform  ·  Hiring Assessment Submission");

        // Tags row
        String[] tags = {"Java 21","Spring Boot 3","Apache PDFBox","Commons CSV","Jackson","Thymeleaf","Bootstrap 5","Maven"};
        float tx = ML;
        float ty = top - 36;
        for (String tag : tags) {
            float tw = strW(HN, 6.5f, tag) + 10;
            roundRect(cb, tx, ty - 1, tw, 11, 3, BG_TAG, ACCENT_S, 0.4f);
            txt(cb, HN, 6.5f, ACCENT, tx + 5, ty + 7, tag);
            tx += tw + 4;
        }

        // One-line summary — right-aligned
        txt(cb, HI, 7f, LIGHT, PW - MR - 310, top - 36,
            "Transforms a Recruiter CSV + Resume PDF into an AI-analysed candidate profile, rendered in-browser and downloadable as a structured JSON report.");
    }

    // =========================================================================
    // LEFT COLUMN
    // =========================================================================
    private float drawLeftColumn(PdfContentByte cb, float x, float top, float bottom) throws Exception {
        float y = top;

        // ── PROBLEM STATEMENT ─────────────────────────────────────────────────
        y = sectionHead(cb, x, y, LC, "Problem Statement");
        String[] prob = {
            "Recruiters manually cross-reference spreadsheets with unstructured resume PDFs before shortlisting — a slow, error-prone process.",
            "This platform automates the pipeline: parse both sources, merge them into one verified candidate record, analyse domain fit, and render a shareable profile.",
            "The result is a recruiter-ready profile page and a structured JSON report — without any manual data entry.",
        };
        y = bodyBlock(cb, x, y, LC, prob, 9.5f);

        y -= 7;

        // ── TECH STACK + FEATURES (side by side) ─────────────────────────────
        y = sectionHead(cb, x, y, LC, "Technology Stack");

        // Two sub-columns inside left column
        float half = (LC - 6) / 2f;
        float lx2 = x, rx2 = x + half + 6;
        float yL = y, yR = y;

        String[] stack = {"Java 21","Spring Boot 3","Apache PDFBox 2.0","Commons CSV 1.10","Jackson Databind","Thymeleaf","Bootstrap 5","Maven"};
        for (int i = 0; i < stack.length; i++) {
            float bx = (i % 2 == 0) ? lx2 : rx2;
            float by = (i % 2 == 0) ? yL : yR;
            float bw = strW(HB, 6.5f, stack[i]) + 10;
            roundRect(cb, bx, by - 2.5f, bw, 11, 2.5f, BG_TAG, ACCENT_S, 0.4f);
            txt(cb, HB, 6.5f, ACCENT, bx + 5, by + 6, stack[i]);
            if (i % 2 == 0) yL -= 14; else yR -= 14;
        }
        y = Math.min(yL, yR) - 4;

        y -= 7;

        // ── KEY FEATURES ─────────────────────────────────────────────────────
        y = sectionHead(cb, x, y, LC, "Key Features");
        String[] feats = {
            "CSV Upload + Resume PDF Upload",
            "Section-based PDF parsing",
            "Data normalisation (name / email / phone)",
            "CSV + Resume content merge",
            "Role-cluster skill scoring (9 domains)",
            "Profile completeness + match score",
            "Recruiter profile UI (Thymeleaf)",
            "Structured JSON report export",
        };
        float fyL = y, fyR = y;
        for (int i = 0; i < feats.length; i++) {
            boolean isLeft = (i % 2 == 0);
            float fx = isLeft ? x : x + half + 6;
            float fy = isLeft ? fyL : fyR;
            txt(cb, HN, 6.5f, GREEN, fx, fy, "\u2713");
            txt(cb, HN, 6.5f, DARK, fx + 9, fy, feats[i]);
            if (isLeft) fyL -= 11; else fyR -= 11;
        }
        y = Math.min(fyL, fyR) - 4;

        y -= 7;

        // ── EDGE CASES ────────────────────────────────────────────────────────
        y = sectionHead(cb, x, y, LC, "Edge Cases Handled");
        String[] edges = {
            "Fresher resume \u2014 experience = 0, labelled \"Fresher\"",
            "No Skills section \u2014 returns empty skill list, no guessing",
            "Certifications after Skills \u2014 stop condition fires immediately",
            "Missing email or phone \u2014 stored as empty string, no null error",
            "Duplicate skills \u2014 deduplicated and sorted alphabetically",
            "Missing CSV column \u2014 empty-string fallback, no crash",
        };
        for (String e : edges) {
            txt(cb, HN, 6f, ACCENT, x, y, "\u25AA");
            txt(cb, HN, 6.5f, SLATE, x + 8, y, e);
            y -= 10;
        }

        return y;
    }

    // =========================================================================
    // RIGHT COLUMN
    // =========================================================================
    private void drawRightColumn(PdfContentByte cb, float x, float top, float bottom) throws Exception {
        float y = top;

        // ── ARCHITECTURE DIAGRAM ──────────────────────────────────────────────
        y = sectionHead(cb, x, y, RC, "System Architecture");
        y = drawArchDiagram(cb, x, y);

        y -= 8;

        // ── DESIGN DECISIONS ─────────────────────────────────────────────────
        y = sectionHead(cb, x, y, RC, "Design Decisions");
        Object[][] dec = {
            {"Section-based PDF parsing",
             "Splitting the resume by heading prevents certifications and project descriptions from contaminating the skills list."},
            {"Technical skill whitelist",
             "Matching only against a curated ~120-entry technology list eliminates internship titles, certificate names, and random text as false positives."},
            {"CSV + Resume merge strategy",
             "CSV supplies verified contact metadata; the resume supplies unstructured content. Combining both produces a richer record than either source alone."},
            {"Rule-based role scoring",
             "Scoring candidate skills against predefined domain clusters runs entirely in-process with no external API — deterministic and always available."},
            {"Separate JSON report object",
             "The downloadable JSON is assembled from parsed data into recruiter-readable sections, keeping the internal model decoupled from the output format."},
        };
        // Two sub-columns for decisions
        float dHalf = (RC - 8) / 2f;
        float dlx = x, drx = x + dHalf + 8;
        float dyL = y, dyR = y;
        for (int i = 0; i < dec.length; i++) {
            boolean isLeft = (i % 2 == 0);
            float dx = isLeft ? dlx : drx;
            float dy = isLeft ? dyL : dyR;
            float dw = dHalf;
            txt(cb, HB, 6.5f, DARK, dx, dy, "\u25B6  " + (String)dec[i][0]);
            dy -= 9;
            // wrap description
            String[] wrapped = wrapText(HN, 6f, (String)dec[i][1], dw - 8);
            for (String wl : wrapped) {
                txt(cb, HN, 6f, SLATE, dx + 8, dy, wl);
                dy -= 8.5f;
            }
            dy -= 3;
            if (isLeft) dyL = dy; else dyR = dy;
        }
        y = Math.min(dyL, dyR) - 4;

        y -= 6;

        // ── FUTURE IMPROVEMENTS ───────────────────────────────────────────────
        y = sectionHead(cb, x, y, RC, "Future Improvements");
        String[][] future = {
            {"Job Description Matching", "Score each candidate against a pasted JD for ranked fit percentages."},
            {"LLM Candidate Summary",    "Replace rule-based summary with GPT-generated professional narrative."},
            {"Batch Resume Processing",  "Support uploading multiple PDFs in a single recruiter session."},
        };
        for (String[] f : future) {
            txt(cb, HB, 6.5f, ACCENT, x, y, "\u2192  " + f[0]);
            txt(cb, HN, 6f, SLATE, x + strW(HB, 6.5f, "\u2192  " + f[0]) + 6, y, f[1]);
            y -= 11;
        }
    }

    // =========================================================================
    // ARCHITECTURE DIAGRAM — drawn on canvas with grouped container
    // =========================================================================
    private float drawArchDiagram(PdfContentByte cb, float x, float y) throws Exception {
        float bw = 130, bh = 15, ax = x + (RC - bw) / 2f;
        float gap = 4f;

        // Helper: draw one diagram box and return new y
        // Recruiter (actor)
        roundRect(cb, ax, y - bh, bw, bh, 3, NAVY, NAVY, 0.5f);
        txtC(cb, HB, 7f, WHITE,  ax + bw/2, y - bh + 5, "Recruiter");
        txtC(cb, HN, 5.5f, new Color(148,163,184), ax + bw/2, y - bh + 0.5f, "Upload CSV + Resume PDF");
        y -= bh + gap;

        // Down arrow
        arrowDown(cb, ax + bw/2, y, y - 6);
        y -= 8;

        // ── Spring Boot Backend container ─────────────────────────────────────
        float cntH = 80f;
        float cntY = y - cntH;
        roundRect(cb, ax - 6, cntY - 3, bw + 12, cntH + 6, 4,
            new Color(246,249,255), ACCENT_S, 0.6f);
        txt(cb, HB, 5f, ACCENT, ax - 4, cntY + cntH - 1, "Spring Boot Backend");

        // Transform Controller
        roundRect(cb, ax, y - bh, bw, bh, 3, new Color(239,246,255), ACCENT_S, 0.4f);
        txtC(cb, HB, 6.5f, DARK, ax + bw/2, y - bh + 5, "Transform Controller");
        txtC(cb, HN, 5f, MID,  ax + bw/2, y - bh + 1, "POST /transform");
        y -= bh + gap;

        arrowDown(cb, ax + bw/2, y, y - 6);  y -= 8;

        // Transform Service
        roundRect(cb, ax, y - bh, bw, bh, 3, new Color(239,246,255), ACCENT_S, 0.4f);
        txtC(cb, HB, 6.5f, DARK, ax + bw/2, y - bh + 5, "Transform Service");
        txtC(cb, HN, 5f, MID,  ax + bw/2, y - bh + 1, "Orchestration layer");
        y -= bh + gap;

        // Fork to parsers
        arrowDown(cb, ax + bw/2, y, y - 6);  y -= 8;

        float half2 = (bw - 4) / 2f;
        float csvX = ax, rsmX = ax + half2 + 4;

        // CSV Parser
        roundRect(cb, csvX, y - bh, half2, bh, 3, BG_CARD, HAIRLINE, 0.4f);
        txtC(cb, HB, 6f, DARK, csvX + half2/2, y - bh + 5.5f, "CSV Parser");
        txtC(cb, HN, 5f, MID,  csvX + half2/2, y - bh + 1, "Commons CSV");

        // Resume Parser
        roundRect(cb, rsmX, y - bh, half2, bh, 3, BG_CARD, HAIRLINE, 0.4f);
        txtC(cb, HB, 6f, DARK, rsmX + half2/2, y - bh + 5.5f, "Resume Parser");
        txtC(cb, HN, 5f, MID,  rsmX + half2/2, y - bh + 1, "PDFBox + Whitelist");

        // Fork lines from center
        cb.setColorStroke(LIGHT); cb.setLineWidth(0.6f);
        cb.moveTo(ax + bw/2, y); cb.lineTo(ax + bw/2, y - 5); cb.stroke();
        cb.moveTo(csvX + half2/2, y - 5); cb.lineTo(rsmX + half2/2, y - 5); cb.stroke();
        arrowDown(cb, csvX + half2/2, y - 5, y - bh);
        arrowDown(cb, rsmX + half2/2, y - 5, y - bh);
        y -= bh + gap;

        // end container here — draw merge line
        arrowDown(cb, ax + bw/2, y, y - 6);  y -= 8;

        // Normalizer + Merger
        roundRect(cb, ax, y - bh, bw, bh, 3, BG_CARD, HAIRLINE, 0.4f);
        txtC(cb, HB, 6.5f, DARK, ax + bw/2, y - bh + 5, "Normalizer  \u00B7  Merger");
        txtC(cb, HN, 5f, MID,  ax + bw/2, y - bh + 1, "Canonical data + field merge");
        y -= bh + gap;

        arrowDown(cb, ax + bw/2, y, y - 6);  y -= 8;

        // AI Analyzer
        roundRect(cb, ax, y - bh, bw, bh, 3, new Color(239,246,255), ACCENT_S, 0.4f);
        txtC(cb, HB, 6.5f, DARK, ax + bw/2, y - bh + 5, "AI Analyzer");
        txtC(cb, HN, 5f, MID,  ax + bw/2, y - bh + 1, "Role \u00B7 Score \u00B7 Gaps \u00B7 Summary");
        y -= bh + gap;

        // Fork to two outputs
        arrowDown(cb, ax + bw/2, y, y - 6);  y -= 8;

        float outW = (bw - 4) / 2f;
        float outL = ax, outR = ax + outW + 4;

        roundRect(cb, outL, y - bh, outW, bh, 3, GRN_BG, GRN_BD, 0.4f);
        txtC(cb, HB, 5.5f, DARK, outL + outW/2, y - bh + 5.5f, "Candidate Profile");
        txtC(cb, HN, 4.5f, MID,  outL + outW/2, y - bh + 1, "Thymeleaf View");

        roundRect(cb, outR, y - bh, outW, bh, 3, GRN_BG, GRN_BD, 0.4f);
        txtC(cb, HB, 5.5f, DARK, outR + outW/2, y - bh + 5.5f, "JSON Report");
        txtC(cb, HN, 4.5f, MID,  outR + outW/2, y - bh + 1, "GET /download/json");

        cb.moveTo(ax + bw/2, y); cb.lineTo(ax + bw/2, y - 5); cb.stroke();
        cb.moveTo(outL + outW/2, y - 5); cb.lineTo(outR + outW/2, y - 5); cb.stroke();
        arrowDown(cb, outL + outW/2, y - 5, y - bh);
        arrowDown(cb, outR + outW/2, y - 5, y - bh);

        y -= bh + 2;

        // ── Horizontal workflow strip ──────────────────────────────────────────
        y -= 10;
        sectionHeadRaw(cb, x, y, RC, "End-to-End Workflow");
        y -= 14;

        String[][] wf = {
            {"1","Upload"},{"2","Parse"},{"3","Normalise"},
            {"4","Merge"}, {"5","Analyse"},{"6","Profile"},{"7","Export"},
        };
        float tileW = (RC - 6 * 5f) / 7f;  // 7 tiles with 5pt gaps
        float tileH = 22f;
        float tx = x;
        for (int i = 0; i < wf.length; i++) {
            // tile background
            boolean isLast = (i == wf.length - 1);
            Color tileBg = isLast ? GRN_BG : (i == 0 ? BG_TAG : WHITE);
            Color tileBd = isLast ? GRN_BD : HAIRLINE;
            roundRect(cb, tx, y - tileH, tileW, tileH, 3, tileBg, tileBd, 0.4f);
            // number
            txtC(cb, HB, 7f, ACCENT, tx + tileW/2, y - 9, wf[i][0]);
            // label
            txtC(cb, HN, 5.5f, DARK, tx + tileW/2, y - 17, wf[i][1]);
            // arrow between tiles
            if (i < wf.length - 1) {
                float arx = tx + tileW + 2f;
                txt(cb, HN, 7f, LIGHT, arx - 1, y - tileH/2 - 4, "\u203A");
            }
            tx += tileW + 5f;
        }
        y -= tileH + 2;

        return y;
    }

    // =========================================================================
    // SCREEN MOCKUPS — drawn accurately from upload.html and index.html
    // =========================================================================
    private void drawScreenshots(PdfContentByte cb, float x, float xEnd,
                                  float top, float bottom) throws Exception {
        float totalW = xEnd - x;
        float ssW = (totalW - 10) / 2f;
        float ssH = top - bottom - 8;
        float ss1X = x, ss2X = x + ssW + 10;

        // ── Screenshot 1: Upload Page ─────────────────────────────────────────
        roundRect(cb, ss1X, bottom, ssW, ssH, 4, WHITE, HAIRLINE, 0.5f);

        // Browser chrome bar
        roundRect(cb, ss1X, bottom + ssH - 10, ssW, 10, 4, BG_CARD, HAIRLINE, 0.4f);
        circle(cb, ss1X + 7,  bottom + ssH - 5, 2.5f, new Color(250,100,100));
        circle(cb, ss1X + 13, bottom + ssH - 5, 2.5f, new Color(250,200,50));
        circle(cb, ss1X + 19, bottom + ssH - 5, 2.5f, new Color(80,190,80));
        // URL bar
        roundRect(cb, ss1X + 26, bottom + ssH - 8.5f, ssW - 38, 7, 2, WHITE, HAIRLINE, 0.4f);
        txt(cb, HN, 4.5f, LIGHT, ss1X + 30, bottom + ssH - 5.5f, "localhost:8080/");

        // Upload card
        float cardX = ss1X + 10, cardW = ssW - 20;
        float cardY = bottom + 8, cardH = ssH - 25;
        roundRect(cb, cardX, cardY, cardW, cardH, 3, WHITE, HAIRLINE, 0.4f);

        // App icon + title
        roundRect(cb, cardX + cardW/2 - 10, cardY + cardH - 16, 20, 14, 3, ACCENT, ACCENT, 0.5f);
        txtC(cb, HB, 6f, WHITE, cardX + cardW/2, cardY + cardH - 10, "\u2737");
        txtC(cb, HB, 6f, NAVY,  cardX + cardW/2, cardY + cardH - 22, "Candidate Transformer AI");

        // CSV upload box
        float ub1Y = cardY + cardH - 38;
        roundRect(cb, cardX + 5, ub1Y - 14, cardW - 10, 14, 2, BG_CARD, HAIRLINE, 0.4f);
        txt(cb, HN, 5f, MID, cardX + 8, ub1Y - 6, "\uD83D\uDCC4  Recruiter CSV");
        txt(cb, HN, 4.5f, LIGHT, cardX + 8, ub1Y - 12, "Drag & drop or click to select");

        // PDF upload box
        float ub2Y = ub1Y - 20;
        roundRect(cb, cardX + 5, ub2Y - 14, cardW - 10, 14, 2, BG_CARD, HAIRLINE, 0.4f);
        txt(cb, HN, 5f, MID, cardX + 8, ub2Y - 6, "\uD83D\uDCC5  Candidate Resume PDF");
        txt(cb, HN, 4.5f, LIGHT, cardX + 8, ub2Y - 12, "Drag & drop or click to select");

        // Generate button
        float btnY = ub2Y - 26;
        roundRect(cb, cardX + 5, btnY, cardW - 10, 12, 2, ACCENT, ACCENT, 0.5f);
        txtC(cb, HB, 6f, WHITE, cardX + cardW/2, btnY + 4.5f, "\u2605  Generate Candidate Profile");

        // Caption
        txtC(cb, HN, 5.5f, MID, ss1X + ssW/2, bottom - 2, "Upload Interface  (upload.html)");

        // ── Screenshot 2: Candidate Profile ──────────────────────────────────
        roundRect(cb, ss2X, bottom, ssW, ssH, 4, WHITE, HAIRLINE, 0.5f);

        // Browser chrome
        roundRect(cb, ss2X, bottom + ssH - 10, ssW, 10, 4, BG_CARD, HAIRLINE, 0.4f);
        circle(cb, ss2X + 7,  bottom + ssH - 5, 2.5f, new Color(250,100,100));
        circle(cb, ss2X + 13, bottom + ssH - 5, 2.5f, new Color(250,200,50));
        circle(cb, ss2X + 19, bottom + ssH - 5, 2.5f, new Color(80,190,80));
        roundRect(cb, ss2X + 26, bottom + ssH - 8.5f, ssW - 38, 7, 2, WHITE, HAIRLINE, 0.4f);
        txt(cb, HN, 4.5f, LIGHT, ss2X + 30, bottom + ssH - 5.5f, "localhost:8080 — Candidate Profile");

        // Top nav bar
        roundRect(cb, ss2X, bottom + ssH - 20, ssW, 10, 0, NAVY, NAVY, 0.5f);
        txt(cb, HB, 5f, WHITE, ss2X + 5, bottom + ssH - 14, "\u25A0  Candidate Transformer AI");
        roundRect(cb, ss2X + ssW - 35, bottom + ssH - 18, 15, 6, 2, ACCENT, ACCENT, 0.5f);
        txt(cb, HN, 4f, WHITE, ss2X + ssW - 33, bottom + ssH - 14, "\u2913 JSON");

        float cly = bottom + ssH - 32;

        // Profile header card
        roundRect(cb, ss2X + 4, cly - 18, ssW - 8, 18, 2, WHITE, HAIRLINE, 0.4f);
        circle(cb, ss2X + 14, cly - 9, 8, ACCENT_S);
        txt(cb, HB, 6f, NAVY, ss2X + 25, cly - 5.5f, "John Doe");
        txt(cb, HN, 5f, ACCENT, ss2X + 25, cly - 11.5f, "Java Backend Developer");
        txt(cb, HN, 4.5f, LIGHT, ss2X + 25, cly - 16.5f, "\u2709 john@email.com   \u260E +91 9876543210");
        // Score ring
        float ringX = ss2X + ssW - 20, ringY = cly - 9;
        circle(cb, ringX, ringY, 9, ACCENT_S);
        circle(cb, ringX, ringY, 6, WHITE);
        txtC(cb, HB, 5.5f, ACCENT, ringX, ringY - 2, "87%");
        cly -= 22;

        // Summary card
        roundRect(cb, ss2X + 4, cly - 11, ssW - 8, 11, 2, WHITE, HAIRLINE, 0.4f);
        txt(cb, HB, 5f, MID, ss2X + 7, cly - 4, "Professional Summary");
        txt(cb, HN, 4.5f, SLATE, ss2X + 7, cly - 9, "B.Tech graduate with hands-on experience, skilled in Java, Spring Boot, React...");
        cly -= 14;

        // Skills card
        roundRect(cb, ss2X + 4, cly - 18, ssW - 8, 18, 2, WHITE, HAIRLINE, 0.4f);
        txt(cb, HB, 5f, MID, ss2X + 7, cly - 5, "Technical Skills   (8 skills)");
        String[] pills = {"Java","Spring Boot","React","MySQL","Docker","Git","REST API","Linux"};
        float px = ss2X + 7; float py = cly - 13;
        for (String p : pills) {
            float pw2 = strW(HN, 4.5f, p) + 6;
            if (px + pw2 > ss2X + ssW - 8) { px = ss2X + 7; py -= 8; }
            roundRect(cb, px, py, pw2, 7, 1.5f, BG_TAG, ACCENT_S, 0.3f);
            txt(cb, HN, 4.5f, ACCENT, px + 3, py + 2, p);
            px += pw2 + 3;
        }
        cly -= 21;

        // AI Insights card
        roundRect(cb, ss2X + 4, cly - 22, ssW - 8, 22, 2, WHITE, HAIRLINE, 0.4f);
        txt(cb, HB, 5f, MID, ss2X + 7, cly - 5, "\uD83E\uDD16  AI Recruiter Insights");
        // 2 mini panels
        float piW = (ssW - 18) / 2f;
        roundRect(cb, ss2X + 6, cly - 20, piW, 14, 2, GRN_BG, GRN_BD, 0.3f);
        txt(cb, HB, 4f, GREEN, ss2X + 8, cly - 11, "Strengths");
        txt(cb, HN, 4f, SLATE, ss2X + 8, cly - 17, "Strong Java & Spring  \u00B7  Backend API");
        roundRect(cb, ss2X + 8 + piW, cly - 20, piW, 14, 2, AMB_BG, new Color(253,230,138), 0.3f);
        txt(cb, HB, 4f, AMBER, ss2X + 10 + piW, cly - 11, "Skill Gaps");
        txt(cb, HN, 4f, SLATE, ss2X + 10 + piW, cly - 17, "MySQL  \u00B7  REST API");
        cly -= 26;

        // Caption
        txtC(cb, HN, 5.5f, MID, ss2X + ssW/2, bottom - 2, "Candidate Profile  (index.html)");
    }

    // =========================================================================
    // FOOTER
    // =========================================================================
    private void drawFooter(PdfContentByte cb) throws Exception {
        hline(cb, ML, PW - MR, MB + 8, HAIRLINE, 0.4f);
        txt(cb, HN, 5.5f, LIGHT, ML, MB + 3,
            "Candidate Transformer AI  \u00B7  AI-Powered Candidate Intelligence Platform  \u00B7  Spring Boot 3 / Java 21");
        txtR(cb, HN, 5.5f, LIGHT, PW - MR, MB + 3,
            "Design Document  \u00B7  Software Engineering Hiring Assessment");
    }

    // =========================================================================
    // PRIMITIVE DRAWING HELPERS
    // =========================================================================

    /** Horizontal rule */
    private void hline(PdfContentByte cb, float x1, float x2, float y,
                       Color color, float w) {
        cb.setColorStroke(color); cb.setLineWidth(w);
        cb.moveTo(x1, y); cb.lineTo(x2, y); cb.stroke();
    }

    /** Filled + stroked rounded rectangle */
    private void roundRect(PdfContentByte cb, float x, float y, float w, float h,
                            float r, Color fill, Color stroke, float sw) {
        cb.setColorFill(fill); cb.setColorStroke(stroke); cb.setLineWidth(sw);
        cb.roundRectangle(x, y, w, h, r);
        cb.fillStroke();
    }

    /** Circle */
    private void circle(PdfContentByte cb, float cx, float cy, float r, Color fill) {
        cb.setColorFill(fill); cb.setColorStroke(fill); cb.setLineWidth(0.2f);
        cb.circle(cx, cy, r);
        cb.fillStroke();
    }

    /** Downward arrow from y1 to y2 at x */
    private void arrowDown(PdfContentByte cb, float x, float y1, float y2) {
        cb.setColorStroke(LIGHT); cb.setColorFill(LIGHT); cb.setLineWidth(0.6f);
        cb.moveTo(x, y1); cb.lineTo(x, y2 + 3); cb.stroke();
        cb.moveTo(x, y2);
        cb.lineTo(x - 3, y2 + 5);
        cb.lineTo(x + 3, y2 + 5);
        cb.closePath(); cb.fill();
    }

    // ── Text helpers ──────────────────────────────────────────────────────────

    /** Left-aligned text */
    private void txt(PdfContentByte cb, BaseFont bf, float sz, Color c,
                     float x, float y, String text) throws Exception {
        cb.beginText();
        cb.setFontAndSize(bf, sz);
        cb.setColorFill(c);
        cb.showTextAligned(Element.ALIGN_LEFT, text, x, y, 0);
        cb.endText();
    }

    /** Center-aligned text */
    private void txtC(PdfContentByte cb, BaseFont bf, float sz, Color c,
                      float cx, float y, String text) throws Exception {
        cb.beginText();
        cb.setFontAndSize(bf, sz);
        cb.setColorFill(c);
        cb.showTextAligned(Element.ALIGN_CENTER, text, cx, y, 0);
        cb.endText();
    }

    /** Right-aligned text */
    private void txtR(PdfContentByte cb, BaseFont bf, float sz, Color c,
                      float x, float y, String text) throws Exception {
        cb.beginText();
        cb.setFontAndSize(bf, sz);
        cb.setColorFill(c);
        cb.showTextAligned(Element.ALIGN_RIGHT, text, x, y, 0);
        cb.endText();
    }

    /** Approximate string width in points */
    private float strW(BaseFont bf, float sz, String text) {
        return bf.getWidthPoint(text, sz);
    }

    // ── Section helpers ────────────────────────────────────────────────────────

    /** Draws a section heading with blue underline. Returns new y below heading. */
    private float sectionHead(PdfContentByte cb, float x, float y,
                               float w, String label) throws Exception {
        y -= 3;
        txt(cb, HB, 7f, ACCENT, x, y, label.toUpperCase());
        hline(cb, x, x + w, y - 2, ACCENT, 0.6f);
        return y - 6;
    }

    /** Section heading without gap-before (used inside arch diagram area) */
    private void sectionHeadRaw(PdfContentByte cb, float x, float y,
                                 float w, String label) throws Exception {
        txt(cb, HB, 7f, ACCENT, x, y, label.toUpperCase());
        hline(cb, x, x + w, y - 2, ACCENT, 0.6f);
    }

    /** Draws a block of body text lines. Returns new y below block. */
    private float bodyBlock(PdfContentByte cb, float x, float y,
                             float maxW, String[] lines, float leading) throws Exception {
        for (String line : lines) {
            String[] wrapped = wrapText(HN, 7f, line, maxW);
            for (String wl : wrapped) {
                txt(cb, HN, 7f, SLATE, x, y, wl);
                y -= leading;
            }
            y -= 2;
        }
        return y;
    }

    /**
     * Naively wraps a string to fit within maxWidth points.
     * Splits on spaces and accumulates words until the line would overflow.
     */
    private String[] wrapText(BaseFont bf, float sz, String text, float maxW) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String test = cur.length() == 0 ? word : cur + " " + word;
            if (strW(bf, sz, test) > maxW && cur.length() > 0) {
                lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) lines.add(cur.toString());
        return lines.toArray(new String[0]);
    }
}
