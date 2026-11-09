package com.zerohundreds.hunt.service;

import org.springframework.stereotype.Service;

@Service
public class StoryboardImageGenerationService {

    /**
     * Generates an authentic hand-drawn storyboard illustration SVG
     * with high-contrast cinematic pencil sketch aesthetic, architectural perspective,
     * cross-hatching, and dramatic director-framed composition.
     */
    public String generateSketchSvg(String shotType, String cameraAngle, String description, String location, int seed) {
        String cleanType = shotType != null ? shotType : "Wide shot";
        String cleanAngle = cameraAngle != null ? cameraAngle : "Eye Level";
        String cleanDesc = description != null ? description : "Cinematic scene composition.";
        String cleanLoc = location != null ? location.toUpperCase() : "INTERIOR / EXTERIOR";

        int hash = Math.abs((cleanDesc + seed + cleanAngle).hashCode());
        int charX = 220 + (hash % 160);
        int horizonY = 160 + (hash % 60);

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 380' width='100%' height='100%'>");
        sb.append("<defs>");
        sb.append("<pattern id='pencilGrad' width='8' height='8' patternUnits='userSpaceOnUse'>");
        sb.append("<path d='M0 8 L8 0 M0 0 L8 8' stroke='#262a33' stroke-width='0.6' opacity='0.35'/>");
        sb.append("</pattern>");
        sb.append("<linearGradient id='noirBg' x1='0' y1='0' x2='0' y2='1'>");
        sb.append("<stop offset='0%' stop-color='#0f1115'/>");
        sb.append("<stop offset='50%' stop-color='#181b22'/>");
        sb.append("<stop offset='100%' stop-color='#0a0c0f'/>");
        sb.append("</linearGradient>");
        sb.append("<linearGradient id='spotlight' x1='0' y1='0' x2='1' y2='1'>");
        sb.append("<stop offset='0%' stop-color='#ffffff' stop-opacity='0.15'/>");
        sb.append("<stop offset='100%' stop-color='#000000' stop-opacity='0.8'/>");
        sb.append("</linearGradient>");
        sb.append("</defs>");

        // Frame Background
        sb.append("<rect width='640' height='380' fill='url(#noirBg)'/>");
        sb.append("<rect width='640' height='380' fill='url(#pencilGrad)'/>");

        // Director Storyboard Inner Boundary
        sb.append("<rect x='24' y='20' width='592' height='340' fill='none' stroke='#3e4451' stroke-width='1.5' stroke-dasharray='10,4' opacity='0.4'/>");

        // Perspective & Vanishing Point Lines (Architectural Cinematic Sketch)
        sb.append("<line x1='24' y1='20' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.4'/>");
        sb.append("<line x1='616' y1='20' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.4'/>");
        sb.append("<line x1='24' y1='360' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.5'/>");
        sb.append("<line x1='616' y1='360' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.5'/>");

        // Horizon line
        sb.append("<line x1='24' y1='").append(horizonY).append("' x2='616' y2='").append(horizonY).append("' stroke='#5c6370' stroke-width='0.8' stroke-dasharray='4,4' opacity='0.35'/>");

        // Background environmental silhouettes based on shot type
        if (cleanType.toLowerCase().contains("wide") || cleanType.toLowerCase().contains("long")) {
            // Pillars / arches / city silhouette
            sb.append("<path d='M60 40 L60 360 M120 70 L120 330 M520 70 L520 330 M580 40 L580 360' stroke='#3b404d' stroke-width='3' opacity='0.6'/>");
            sb.append("<path d='M60 90 Q320 20 580 90' fill='none' stroke='#4b5263' stroke-width='2' opacity='0.5'/>");
            // Distant character silhouette
            sb.append("<circle cx='").append(charX).append("' cy='").append(horizonY - 25).append("' r='7' fill='#c8ccd4'/>");
            sb.append("<path d='M").append(charX - 10).append(" ").append(horizonY - 18).append(" L").append(charX + 10).append(" ").append(horizonY - 18)
              .append(" L").append(charX + 14).append(" ").append(horizonY + 35).append(" L").append(charX - 14).append(" ").append(horizonY + 35).append(" Z' fill='#9da5b4'/>");
            sb.append("<line x1='").append(charX - 6).append("' y1='").append(horizonY + 35).append("' x2='").append(charX - 6).append("' y2='").append(horizonY + 60).append("' stroke='#abb2bf' stroke-width='3.5'/>");
            sb.append("<line x1='").append(charX + 6).append("' y1='").append(horizonY + 35).append("' x2='").append(charX + 6).append("' y2='").append(horizonY + 60).append("' stroke='#abb2bf' stroke-width='3.5'/>");
        } else if (cleanType.toLowerCase().contains("close")) {
            // Close up dramatic human portrait sketch
            sb.append("<circle cx='320' cy='150' r='55' fill='#21252b' stroke='#c8ccd4' stroke-width='2.5'/>");
            // Hair / contour
            sb.append("<path d='M265 140 Q320 80 375 140 Q380 90 320 85 Q260 90 265 140' fill='#abb2bf'/>");
            // Face features sketch
            sb.append("<path d='M295 140 Q320 145 345 140' stroke='#abb2bf' stroke-width='2' fill='none'/>"); // Eyebrows
            sb.append("<circle cx='302' cy='150' r='4' fill='#fff'/><circle cx='338' cy='150' r='4' fill='#fff'/>"); // Eyes
            sb.append("<path d='M320 150 L318 175 L328 175' stroke='#c8ccd4' stroke-width='2' fill='none'/>"); // Nose
            sb.append("<path d='M308 190 Q320 195 332 190' stroke='#c8ccd4' stroke-width='2.2' fill='none'/>"); // Mouth
            // Shoulders & Trench coat collar
            sb.append("<path d='M230 250 L270 205 L320 230 L370 205 L410 250 L440 360 L200 360 Z' fill='#1e2227' stroke='#828997' stroke-width='2'/>");
            sb.append("<path d='M270 205 L300 280 M370 205 L340 280' stroke='#5c6370' stroke-width='2'/>");
        } else {
            // Over-the-shoulder / Medium shot
            // Foreground silhouette
            sb.append("<path d='M40 180 Q100 130 160 170 L200 360 L20 360 Z' fill='#0f1115' stroke='#3e4451' stroke-width='2.5'/>");
            sb.append("<circle cx='105' cy='140' r='38' fill='#0f1115' stroke='#3e4451' stroke-width='2'/>");
            // Focal subject in midground
            sb.append("<circle cx='").append(charX + 40).append("' cy='160' r='26' fill='#21252b' stroke='#d7dae0' stroke-width='2'/>");
            sb.append("<path d='M").append(charX).append(" 186 Q").append(charX + 40).append(" 176 ").append(charX + 80).append(" 186 L")
              .append(charX + 90).append(" 330 L").append(charX - 10).append(" 330 Z' fill='#2c313a' stroke='#abb2bf' stroke-width='2'/>");
            sb.append("<line x1='").append(charX + 25).append("' y1='330' x2='").append(charX + 25).append("' y2='370' stroke='#c8ccd4' stroke-width='4'/>");
            sb.append("<line x1='").append(charX + 55).append("' y1='330' x2='").append(charX + 55).append("' y2='370' stroke='#c8ccd4' stroke-width='4'/>");
        }

        // Cinematic cross-hatching shading overlay
        sb.append("<path d='M24 320 L160 360 M30 310 L170 350 M36 300 L180 340' stroke='#abb2bf' stroke-width='0.8' opacity='0.3'/>");
        sb.append("<path d='M480 320 L616 360 M490 310 L616 345 M500 300 L616 330' stroke='#abb2bf' stroke-width='0.8' opacity='0.3'/>");

        // Director's Slate Notation on bottom-left of drawing
        sb.append("<rect x='34' y='325' width='220' height='26' rx='3' fill='#0b0d10' fill-opacity='0.85' stroke='#4b5263' stroke-width='0.8'/>");
        sb.append("<text x='42' y='342' font-family='monospace' font-size='10' font-weight='bold' fill='#e5c07b'>")
          .append("DIR SLATE // ").append(cleanType.toUpperCase()).append(" · ").append(cleanAngle.toUpperCase())
          .append("</text>");

        sb.append("</svg>");
        return "data:image/svg+xml;utf8," + java.net.URLEncoder.encode(sb.toString(), java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
