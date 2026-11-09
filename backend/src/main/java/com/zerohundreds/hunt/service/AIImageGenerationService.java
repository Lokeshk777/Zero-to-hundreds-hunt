package com.zerohundreds.hunt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class AIImageGenerationService {

    @Value("${ai.image.api-key:}")
    private String hfApiKey;

    @Value("${ai.image.model:black-forest-labs/FLUX.1-schnell}")
    private String hfModel;

    public static class ImageGenResult {
        public byte[] imageBytes;
        public String svgContent;
        public String provenance; // "AI_GENERATED", "LOCAL_ILLUSTRATION"
        public String status;     // "COMPLETED", "FAILED"
        public String errorMessage;
        public String format;     // "jpg", "png", "svg"
    }

    /**
     * Generates cinematic visual images.
     * Checks Hugging Face Inference API if user has provided HUGGINGFACE_API_KEY.
     * If unconfigured or quota is exhausted, generates high-fidelity local vector illustration
     * and accurately marks provenance as LOCAL_ILLUSTRATION (no false claims).
     */
    public ImageGenResult generateSceneVisual(String shotType, String cameraAngle, String prompt, String location, String characterName, String visualStyle, int seed) {
        ImageGenResult result = new ImageGenResult();

        // 1. Attempt AI Image Generation if Hugging Face API key is present
        if (hfApiKey != null && !hfApiKey.isBlank() && !hfApiKey.equals("YOUR_HF_API_KEY_HERE")) {
            try {
                byte[] bytes = callHuggingFaceImageGen(prompt, visualStyle);
                if (bytes != null && bytes.length > 1000) {
                    result.imageBytes = bytes;
                    result.provenance = "AI_GENERATED";
                    result.status = "COMPLETED";
                    result.format = "jpg";
                    return result;
                }
            } catch (Exception e) {
                System.err.println("[AIImageGenerationService] AI generation notice: " + e.getMessage() + ". Generating local vector drawing.");
                result.errorMessage = "AI Provider quota exceeded or unavailable (" + e.getMessage() + "). Rendered as local illustration.";
            }
        } else {
            result.errorMessage = "External Image API key not set in environment. Rendered as authentic local cinematic drawing.";
        }

        // 2. Local Cinematic Drawing Fallback (Accurately labeled LOCAL_ILLUSTRATION)
        String svg = renderLocalCinematicSvg(shotType, cameraAngle, prompt, location, characterName, visualStyle, seed);
        result.svgContent = svg;
        result.provenance = "LOCAL_ILLUSTRATION";
        result.status = "COMPLETED";
        result.format = "svg";
        return result;
    }

    private byte[] callHuggingFaceImageGen(String prompt, String style) throws Exception {
        String url = "https://api-inference.huggingface.co/models/" + hfModel;
        String fullPrompt = style + ", cinematic lighting, 8k resolution: " + prompt;
        String body = "{\"inputs\":\"" + escapeJson(fullPrompt) + "\"}";

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + hfApiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(45))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<byte[]> res = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        if (res.statusCode() == 429) {
            throw new RuntimeException("HuggingFace Free Quota Exceeded (HTTP 429).");
        }
        if (res.statusCode() != 200) {
            throw new RuntimeException("HuggingFace returned status " + res.statusCode());
        }

        return res.body();
    }

    /**
     * Renders authentic, director-grade cinematic visual sketches
     * customized for character, location, lighting, and camera composition.
     */
    public String renderLocalCinematicSvg(String shotType, String cameraAngle, String desc, String location, String characterName, String style, int seed) {
        String cleanType = shotType != null ? shotType : "Wide shot";
        String cleanAngle = cameraAngle != null ? cameraAngle : "Eye Level";
        String cleanDesc = desc != null ? desc : "Cinematic scene composition";
        String cleanLoc = (location != null ? location : "INTERIOR / EXTERIOR").toUpperCase();
        String cleanChar = (characterName != null ? characterName : "PROTAGONIST").toUpperCase();

        int hash = Math.abs((cleanDesc + seed + cleanAngle + cleanLoc).hashCode());
        int charX = 220 + (hash % 160);
        int horizonY = 160 + (hash % 50);

        boolean isClose = cleanType.toLowerCase().contains("close");
        boolean isOverShoulder = cleanType.toLowerCase().contains("over");
        boolean isNight = cleanDesc.toLowerCase().contains("night") || cleanLoc.contains("NIGHT");

        StringBuilder sb = new StringBuilder();
        sb.append("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 380' width='100%' height='100%'>");
        sb.append("<defs>");
        sb.append("<pattern id='pGrad_").append(seed).append("' width='8' height='8' patternUnits='userSpaceOnUse'>");
        sb.append("<path d='M0 8 L8 0 M0 0 L8 8' stroke='#282c34' stroke-width='0.6' opacity='0.4'/>");
        sb.append("</pattern>");
        sb.append("<linearGradient id='noirBg_").append(seed).append("' x1='0' y1='0' x2='0' y2='1'>");
        sb.append("<stop offset='0%' stop-color='").append(isNight ? "#07080a" : "#0f1115").append("'/>");
        sb.append("<stop offset='50%' stop-color='").append(isNight ? "#0d0f14" : "#191c22").append("'/>");
        sb.append("<stop offset='100%' stop-color='#0a0c0f'/>");
        sb.append("</linearGradient>");
        sb.append("</defs>");

        // Background
        sb.append("<rect width='640' height='380' fill='url(#noirBg_").append(seed).append(")'/>");
        sb.append("<rect width='640' height='380' fill='url(#pGrad_").append(seed).append(")'/>");

        // Director Frame Boundaries
        sb.append("<rect x='18' y='16' width='604' height='348' fill='none' stroke='#3e4451' stroke-width='1.5' stroke-dasharray='10,4' opacity='0.35'/>");
        sb.append("<line x1='18' y1='16' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>");
        sb.append("<line x1='622' y1='16' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.2' opacity='0.35'/>");
        sb.append("<line x1='18' y1='364' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>");
        sb.append("<line x1='622' y1='364' x2='").append(charX).append("' y2='").append(horizonY).append("' stroke='#4f5666' stroke-width='1.5' opacity='0.45'/>");
        sb.append("<line x1='18' y1='").append(horizonY).append("' x2='622' y2='").append(horizonY).append("' stroke='#5c6370' stroke-width='0.8' stroke-dasharray='4,4' opacity='0.35'/>");

        // Environment details based on location
        if (cleanLoc.contains("TEA SHOP") || cleanLoc.contains("CAFE") || cleanLoc.contains("RESTAURANT")) {
            sb.append("<rect x='40' y='100' width='140' height='220' fill='#161922' stroke='#4b5263' stroke-width='1.8'/>");
            sb.append("<line x1='40' y1='180' x2='180' y2='180' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<circle cx='110' cy='150' r='18' fill='#e5c07b' opacity='0.7'/>"); // warm tea lamp
            sb.append("<rect x='460' y='120' width='130' height='200' fill='#12151c' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<line x1='20' y1='300' x2='620' y2='300' stroke='#5c6370' stroke-width='2'/>");
        } else if (cleanLoc.contains("ROOFTOP") || cleanLoc.contains("STREET") || cleanLoc.contains("CITY")) {
            sb.append("<rect x='30' y='80' width='110' height='260' fill='#12151c' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<rect x='470' y='60' width='130' height='280' fill='#141720' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<line x1='30' y1='280' x2='610' y2='280' stroke='#abb2bf' stroke-width='2'/>");
        } else {
            sb.append("<path d='M50 50 L50 350 M120 70 L120 330 M520 70 L520 330 M590 50 L590 350' stroke='#3b404d' stroke-width='2.5' opacity='0.7'/>");
            sb.append("<path d='M50 80 Q320 30 590 80' fill='none' stroke='#4b5263' stroke-width='2' opacity='0.6'/>");
            sb.append("<line x1='20' y1='310' x2='620' y2='310' stroke='#5c6370' stroke-width='2'/>");
        }

        // Character figures
        if (isClose) {
            sb.append("<circle cx='320' cy='135' r='55' fill='#1b1f27' stroke='#abb2bf' stroke-width='2.2'/>");
            sb.append("<path d='M265 130 Q320 65 375 130 Q385 75 320 70 Q255 75 265 130' fill='#5c6370'/>");
            sb.append("<path d='M295 132 Q320 137 345 132' stroke='#abb2bf' stroke-width='2' fill='none'/>");
            sb.append("<circle cx='305' cy='142' r='4' fill='#fff'/><circle cx='335' cy='142' r='4' fill='#fff'/>");
            sb.append("<path d='M320 142 L317 165 L327 165' stroke='#c8ccd4' stroke-width='2' fill='none'/>");
            sb.append("<path d='M308 180 Q320 185 332 180' stroke='#c8ccd4' stroke-width='2.2' fill='none'/>");
            sb.append("<path d='M220 250 L265 195 L320 220 L375 195 L420 250 L450 360 L190 360 Z' fill='#161920' stroke='#5c6370' stroke-width='2'/>");
            sb.append("<text x='320' y='275' font-family='sans-serif' font-size='10' font-weight='bold' fill='#e5c07b' text-anchor='middle'>").append(cleanChar).append("</text>");
        } else if (isOverShoulder) {
            sb.append("<path d='M20 150 Q90 110 140 150 L180 360 L10 360 Z' fill='#0a0c0f' stroke='#3e4451' stroke-width='2'/>");
            sb.append("<circle cx='95' cy='120' r='35' fill='#0a0c0f'/>");
            sb.append("<circle cx='").append(charX).append("' cy='155' r='24' fill='#1b1f27' stroke='#abb2bf' stroke-width='2'/>");
            sb.append("<path d='M").append(charX - 25).append(" 179 L").append(charX + 25).append(" 179 L").append(charX + 30).append(" 310 L").append(charX - 30).append(" 310 Z' fill='#242832' stroke='#5c6370' stroke-width='1.8'/>");
            sb.append("<line x1='").append(charX - 10).append("' y1='310' x2='").append(charX - 10).append("' y2='360' stroke='#828997' stroke-width='3.5'/>");
            sb.append("<line x1='").append(charX + 10).append("' y1='310' x2='").append(charX + 10).append("' y2='360' stroke='#828997' stroke-width='3.5'/>");
            sb.append("<text x='").append(charX).append("' y='245' font-family='sans-serif' font-size='9' font-weight='bold' fill='#e5c07b' text-anchor='middle'>").append(cleanChar).append("</text>");
        } else {
            sb.append("<circle cx='").append(charX).append("' cy='").append(horizonY - 22).append("' r='10' fill='#c8ccd4'/>");
            sb.append("<path d='M").append(charX - 14).append(" ").append(horizonY - 12).append(" L").append(charX + 14).append(" ").append(horizonY - 12)
              .append(" L").append(charX + 18).append(" ").append(horizonY + 45).append(" L").append(charX - 18).append(" ").append(horizonY + 45).append(" Z' fill='#282c34' stroke='#828997' stroke-width='1.8'/>");
            sb.append("<line x1='").append(charX - 7).append("' y1='").append(horizonY + 45).append("' x2='").append(charX - 7).append("' y2='").append(horizonY + 85).append("' stroke='#abb2bf' stroke-width='4'/>");
            sb.append("<line x1='").append(charX + 7).append("' y1='").append(horizonY + 45).append("' x2='").append(charX + 7).append("' y2='").append(horizonY + 85).append("' stroke='#abb2bf' stroke-width='4'/>");
            sb.append("<text x='").append(charX).append("' y='").append(horizonY + 22).append("' font-family='sans-serif' font-size='9' font-weight='bold' fill='#e5c07b' text-anchor='middle'>").append(cleanChar).append("</text>");
        }

        // Director's Slate Notation on bottom-left of drawing
        sb.append("<rect x='28' y='325' width='320' height='26' rx='3' fill='#0b0d10' fill-opacity='0.9' stroke='#4b5263' stroke-width='0.8'/>");
        sb.append("<text x='36' y='342' font-family='monospace' font-size='10' font-weight='bold' fill='#e5c07b'>")
          .append("LOCAL DRAWING // ").append(cleanType.toUpperCase()).append(" · ").append(cleanLoc.length() > 18 ? cleanLoc.substring(0, 18) : cleanLoc)
          .append("</text>");

        sb.append("</svg>");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
