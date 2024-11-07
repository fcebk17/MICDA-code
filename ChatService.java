package OpenAIServiceHub.OpenAIServiceHub.service;
import OpenAIServiceHub.OpenAIServiceHub.config.ProjectInfoConfig;
import OpenAIServiceHub.OpenAIServiceHub.entity.Clusters;
import OpenAIServiceHub.OpenAIServiceHub.entity.TextGeneration;
import OpenAIServiceHub.OpenAIServiceHub.repository.ClustersRepository;
import OpenAIServiceHub.OpenAIServiceHub.repository.EndpointsRepository;
import OpenAIServiceHub.OpenAIServiceHub.repository.TextGenerationRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONObject;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatService {

    private final OpenAiChatClient client;

    @Autowired
    public ChatService(OpenAiChatClient client) {
        this.client = client;
    }

    @Autowired
    private EndpointsRepository endpointsRepository;
    @Autowired
    private TextGenerationRepository generationRepository;
    @Autowired
    private ClustersRepository clustersRepository;
    @Autowired
    private ProjectInfoConfig config;

    private ChatResponse promptGenerateClustering(String concept,String endpointList,String endpointListSize) {
        List list = new ArrayList<>();

        String template = "### Code: \n{code}###";
        PromptTemplate promptTemplateCode = new PromptTemplate(template, Map.of("code", endpointList));
        list.add(promptTemplateCode.createMessage());

        list.add(new SystemMessage("You are a software engineering researcher."));

        template ="The above is the code extracted from the project, containing {endpointListSize} endpoints.";
        promptTemplateCode = new PromptTemplate(template, Map.of("endpointListSize", endpointListSize));
        list.add(promptTemplateCode.createMessage());

        template ="Based on the {concept} concept, please perform microservice clustering \n" +
                "for the {endpointListSize} endpoints.";
        promptTemplateCode = new PromptTemplate(template, Map.of("concept", concept,"endpointListSize",endpointListSize));
        list.add(promptTemplateCode.createMessage());


        list.add(new UserMessage("Provide clustering suggestions with  \n" +
                "1. Cluster number and name (groupNames)  \n" +
                "2. Endpoint to cluster number mapping (endpointGroupMapping)."));

        String format = "Please reply in this format: ###" +
                "{\n" +
                "  \"groupNames\": {\n" +
                "    \"1\": \"ServiceA\",\n" +
                "    \"2\": \"ServiceB\"\n" +
                "  },\n" +
                "  \"endpointGroupMapping\": {\n" +
                "    \"EndpointA\": 1,\n" +
                "    \"EndpointB\": 2\n" +
                "  }\n" +
                "} ###";
        list.add(new SystemMessage(format));

        Prompt prompt = new Prompt(list);
        ChatResponse response = client.call(prompt);

        return response;
    }
}