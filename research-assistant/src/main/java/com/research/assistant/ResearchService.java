package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ResearchService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ResearchService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }


    public String processContent(ResearchRequest request) {
        // Build the prompt
        String prompt = buildPrompt(request);

        // Query the AI Model API
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[] {
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse the response
        // Return response

        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try {
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if (firstCandidate.getContent() != null &&
                        firstCandidate.getContent().getParts() != null &&
                        !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No content found in response";
        } catch (Exception e) {
            return "Error Parsing: " + e.getMessage();
        }
    }

    private String buildPrompt(ResearchRequest request) {
        StringBuilder prompt = new StringBuilder();
        switch (request.getOperation()) {
            case "summarize":
                prompt.append("Provide a clear and concise summary of the following text in a few sentences:\n\n");
                break;
            case "suggest":
                prompt.append("Based on the following content: suggest related topics and further reading. Format the response with clear headings and bullet points:\n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}

//This Java code snippet defines a method called buildPrompt that generates a prompt string for a research request.  Let's break down the code step by step:
//
//        1. Method Signature:
//
//Java
//
//private String buildPrompt(ResearchRequest request) {
//    private: This keyword indicates that the method is only accessible within the same class where it's defined. It's a common access modifier for helper methods.
//    String: This specifies that the method returns a String value. The generated prompt will be a string.
//            buildPrompt: This is the name of the method. It clearly suggests the purpose of the method: to build a prompt.
//    ResearchRequest request: This declares a parameter named request of type ResearchRequest. This implies that there's a separate class called ResearchRequest which encapsulates the details of a research request (likely containing information like the desired operation and the content to be processed).
//    2.  StringBuilder Initialization:
//
//    Java
//
//    StringBuilder prompt = new StringBuilder();
//    StringBuilder: This class is used to efficiently build strings, especially when you need to concatenate strings multiple times. Using StringBuilder is generally much more performant than repeatedly using the + operator for string concatenation, as it avoids creating numerous intermediate String objects.
//            prompt: This is a variable of type StringBuilder that will hold the prompt being constructed.
//    new StringBuilder(): This creates a new, empty StringBuilder object.
//    3. Switch Statement Based on Operation:
//
//    Java
//
//    switch (request.getOperation()){
//        case "summarize":
//            prompt.append("Provide a clear and concise summary of the following text in a few sentences ");
//            break;
//
//        case "suggest":
//            prompt.append("Based on the following content: suggest related topics and further reading. Format the response with clear headings and bullet points:\n\n");
//            break;
//        default:
//            throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
//
//    }
//    switch (request.getOperation()): This switch statement checks the value of the operation field of the request object. The getOperation() method is assumed to be a getter method defined in the ResearchRequest class. This field likely specifies what kind of research operation is desired (e.g., "summarize," "suggest," etc.).
//    case "summarize": If the operation is "summarize," the following line is executed.
//            prompt.append("Provide a clear and concise summary of the following text in a few sentences ");: This appends the instruction to summarize to the prompt StringBuilder.
//    break: This is crucial in a switch statement. It prevents the code from "falling through" to the next case.
//    case "suggest": If the operation is "suggest," the following line is executed.
//            prompt.append("Based on the following content: suggest related topics and further reading. Format the response with clear headings and bullet points:\n\n");: This appends the instruction to suggest related topics and further reading, along with formatting instructions, to the prompt StringBuilder. \n\n adds two newline characters for better formatting.
//    break: Prevents fallthrough.
//    default: This case is executed if the operation value doesn't match any of the defined case values.
//    throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());: This throws an IllegalArgumentException to indicate that an invalid operation was requested. This is good practice for error handling.
//    4. Appending the Content:
//
//    Java
//
//    prompt.append(request.getContent());
//    request.getContent(): This retrieves the content to be processed from the request object (likely using a getter method).
//            prompt.append(...): This appends the retrieved content to the prompt StringBuilder. This is where the actual text that needs to be summarized or used for suggestions is added to the prompt.
//    5. Returning the Prompt:
//
//    Java
//
//    return prompt.toString();
//    prompt.toString(): This converts the StringBuilder object to a regular String object.
//    return: This returns the complete, built prompt string.