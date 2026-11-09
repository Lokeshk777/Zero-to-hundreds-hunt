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

        int hash = Math.abs((cleanDesc + seed + cleanAngle + cleanLoc).hashCode());
        int charX = 220 + (hash % 160);
        int horizonY = 160 + (hash % 50);

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 380' width='100%' height='100%'>");
        sb.append("<defs>");
        sb.append("<pattern id='pencilGrad_").append(seed).append("' width='8' height='8' patternUnits='userSpaceOnUse'>");
        sb.append("<path d='M0 8 L8 0 M0 0 L8 8' stroke='#262a33' stroke-width='0.6' opacity='0.35'/>");
        sb.append("</pattern>");
        sb.append("<linearGradient id='noirBg_").append(seed).append("' x1='0' y1='0' x2='0' y2='1'>");
        sb.append("<stop offset='0%' stop-color='#0f1115'/>");
        sb.append("<stop offset='50%' stop-color='#181b22'/>");
        sb.append("<stop offset='100%' stop-color='#0a0c0f'/>");
        sb.append("</linearGradient>");
        sb.append("</defs>");

        // Frame Background
        sb.append("<rect width='640' height='380' fill='url(#noirBg_").append(seed).append(")'/>");
        sb.append("<rect width='640' height='380' fill='url(#pencilGrad_").append(seed).append(")'/>");

        // Director Storyboard Inner Boundary
        sb.append("<rect x='20' y='18' width='600' height='344' fill='none' stroke='#3e4451' stroke-width='1.5' stroke-dasharray='10,4' opacity='0.4'/>");

        // Perspective Lines
        sb.append("<line x1='20' y1='18' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>");
        sb.append("<line x1='620' y1='18' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>");
        sb.append("<line x1='20' y1='362' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>");
        sb.append("<line x1='620' y1='362' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>");
        sb.append("<line x1='20' y1='").append(horizonY).append("' x2='620' y2='").append(horizonY).append("' stroke='#5c6370' stroke-width='0.8' stroke-dasharray='4,4' opacity='0.35'/>");

        // Environment silhouettes based on story location
        if (cleanLoc.contains("HOSPITAL") || cleanLoc.contains("CLINIC") || cleanLoc.contains("LAB")) {
            sb.append("<rect x='40' y='60' width='80' height='260' fill='none' stroke='#4b5263' stroke-width='2'/>");
            sb.append("<line x1='80' y1='60' x2='80' y2='320' stroke='#333842' stroke-width='1.5'/>");
            sb.append("<rect x='480' y='80' width='110' height='160' rx='4' fill='#12151b' stroke='#5c6370' stroke-width='2'/>");
            sb.append("<path d='M495 160 L515 160 L525 130 L540 190 L550 150 L560 165 L575 165' fill='none' stroke='#abb2bf' stroke-width='2'/>");
            sb.append("<line x1='20' y1='300' x2='620' y2='300' stroke='#3e4451' stroke-width='2'/>");
        } else if (cleanLoc.contains("ROOFTOP") || cleanLoc.contains("STREET") || cleanLoc.contains("ALLEY") || cleanLoc.contains("CITY")) {
            sb.append("<rect x='30' y='80' width='120' height='260' fill='#12151c' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<rect x='180' y='120' width='90' height='220' fill='#161922' stroke='#4b5263' stroke-width='1.8'/>");
            sb.append("<rect x='460' y='60' width='140' height='280' fill='#141720' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<line x1='30' y1='280' x2='610' y2='280' stroke='#abb2bf' stroke-width='2'/>");
        } else {
            sb.append("<path d='M60 40 L60 360 M120 70 L120 330 M520 70 L520 330 M580 40 L580 360' stroke='#3b404d' stroke-width='2.5' opacity='0.7'/>");
            sb.append("<path d='M60 90 Q320 20 580 90' fill='none' stroke='#4b5263' stroke-width='2' opacity='0.6'/>");
            sb.append("<line x1='20' y1='310' x2='620' y2='310' stroke='#5c6370' stroke-width='2'/>");
        }

        // Foreground / character silhouettes
        if (cleanType.toLowerCase().contains("close")) {
            sb.append("<circle cx='320' cy='135' r='55' fill='#1b1f27' stroke='#abb2bf' stroke-width='2.2'/>");
            sb.append("<path d='M265 130 Q320 65 375 130 Q385 75 320 70 Q255 75 265 130' fill='#5c6370'/>");
            sb.append("<path d='M295 132 Q320 137 345 132' stroke='#abb2bf' stroke-width='2' fill='none'/>");
            sb.append("<circle cx='305' cy='142' r='4' fill='#fff'/><circle cx='335' cy='142' r='4' fill='#fff'/>");
            sb.append("<path d='M320 142 L317 165 L327 165' stroke='#c8ccd4' stroke-width='2' fill='none'/>");
            sb.append("<path d='M308 180 Q320 185 332 180' stroke='#c8ccd4' stroke-width='2.2' fill='none'/>");
            sb.append("<path d='M220 250 L265 195 L320 220 L375 195 L420 250 L450 360 L190 360 Z' fill='#161920' stroke='#5c6370' stroke-width='2'/>");
        } else if (cleanType.toLowerCase().contains("over")) {
            sb.append("<path d='M20 150 Q90 110 140 150 L180 360 L10 360 Z' fill='#0a0c0f' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<circle cx='95' cy='120' r='35' fill='#0a0c0f'/>");
            sb.append("<circle cx='").append(charX).append("' cy='155' r='24' fill='#1b1f27' stroke='#abb2bf' stroke-width='2'/>");
            sb.append("<path d='M").append(charX - 25).append(" 179 L").append(charX + 25).append(" 179 L").append(charX + 30).append(" 310 L").append(charX - 30).append(" 310 Z' fill='#242832' stroke='#5c6370' stroke-width='1.8'/>");
            sb.append("<line x1='").append(charX - 10).append("' y1='310' x2='").append(charX - 10).append("' y2='360' stroke='#828997' stroke-width='3.5'/>");
            sb.append("<line x1='").append(charX + 10).append("' y1='310' x2='").append(charX + 10).append("' y2='360' stroke='#828997' stroke-width='3.5'/>");
        } else {
            sb.append("<circle cx='").append(charX).append("' cy='").append(horizonY - 22).append("' r='10' fill='#c8ccd4'/>");
            sb.append("<path d='M").append(charX - 14).append(" ").append(horizonY - 12).append(" L").append(charX + 14).append(" ").append(horizonY - 12)
              .append(" L").append(charX + 18).append(" ").append(horizonY + 45).append(" L").append(charX - 18).append(" ").append(horizonY + 45).append(" Z' fill='#282c34' stroke='#828997' stroke-width='1.8'/>");
            sb.append("<line x1='").append(charX - 7).append("' y1='").append(horizonY + 45).append("' x2='").append(charX - 7).append("' y2='").append(horizonY + 85).append("' stroke='#abb2bf' stroke-width='4'/>");
            sb.append("<line x1='").append(charX + 7).append("' y1='").append(horizonY + 45).append("' x2='").append(charX + 7).append("' y2='").append(horizonY + 85).append("' stroke='#abb2bf' stroke-width='4'/>");
        }

        // Director's Slate Notation on bottom-left of drawing
        sb.append("<rect x='28' y='325' width='280' height='26' rx='3' fill='#0b0d10' fill-opacity='0.9' stroke='#4b5263' stroke-width='0.8'/>");
        sb.append("<text x='36' y='342' font-family='monospace' font-size='10' font-weight='bold' fill='#e5c07b'>")
          .append("DIR SLATE // ").append(cleanType.toUpperCase()).append(" · ").append(cleanLoc.length() > 18 ? cleanLoc.substring(0, 18) : cleanLoc)
          .append("</text>");

        sb.append("</svg>");
        return "data:image/svg+xml;utf8," + java.net.URLEncoder.encode(sb.toString(), java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }
}
