package com.eightfold.candidate_transformer_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.eightfold.candidate_transformer_v2.model.Candidate;
import com.eightfold.candidate_transformer_v2.service.TransformService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransformController {

    private final TransformService transformService;

    public TransformController(TransformService transformService) {
        this.transformService = transformService;
    }

    @PostMapping("/transform")
    public String transform(

            @RequestParam("csvFile") MultipartFile csvFile,

            @RequestParam("resumeFile") MultipartFile resumeFile,

            Model model,

            HttpSession session) {

        try {

            Candidate candidate =
                    transformService.transform(csvFile, resumeFile);

            session.setAttribute("candidate", candidate);

            model.addAttribute("candidate", candidate);

            return "index";

        } catch (Exception e) {

            model.addAttribute("error", e.getMessage());

            return "upload";
        }
    }

}