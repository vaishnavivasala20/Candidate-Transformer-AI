/**
 * Candidate Transformer AI — Dashboard JS
 * Handles: score ring animation, completeness bar animation, skill hover
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        animateScoreRing();
        animateCompletenessBar();
        animateSkillsStagger();
    });

    /**
     * Animates the conic-gradient score ring from 0% to the actual match score.
     * Reads the percentage from the .score-value element text.
     */
    function animateScoreRing() {
        const ring       = document.querySelector('.score-ring');
        const scoreLabel = document.querySelector('.score-value');
        if (!ring || !scoreLabel) return;

        const raw    = scoreLabel.textContent.replace('%', '').trim();
        const target = Math.min(Math.max(parseFloat(raw) || 0, 0), 100);

        let current = 0;
        const step  = target / 60; // ~60 frames
        const timer = setInterval(function () {
            current = Math.min(current + step, target);
            ring.style.background =
                'conic-gradient(#2563eb ' + current + '%, #dbeafe ' + current + '%)';
            if (current >= target) clearInterval(timer);
        }, 16);
    }

    /**
     * Animates completeness bars from 0 to their target width.
     */
    function animateCompletenessBar() {
        document.querySelectorAll('.completeness-fill').forEach(function (bar) {
            const target = bar.style.width;
            bar.style.width = '0%';
            // Use requestAnimationFrame to trigger CSS transition
            requestAnimationFrame(function () {
                requestAnimationFrame(function () {
                    bar.style.transition = 'width 1.0s ease';
                    bar.style.width      = target;
                });
            });
        });
    }

    /**
     * Adds a staggered fade-in to skill pills.
     */
    function animateSkillsStagger() {
        const pills = document.querySelectorAll('.skill-pill');
        pills.forEach(function (pill, i) {
            pill.style.opacity   = '0';
            pill.style.transform = 'translateY(8px)';
            setTimeout(function () {
                pill.style.transition = 'opacity 0.3s ease, transform 0.3s ease';
                pill.style.opacity    = '1';
                pill.style.transform  = 'translateY(0)';
            }, 80 + i * 35);
        });
    }

})();
