package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.UserDAO;
import models.User;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import utils.Constants;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

    /**
     * Direct OAuth Service for Desktop Java Applications
     *
     * Implements OAuth 2.0 flow directly with Google and GitHub APIs
     * Pure OAuth implementation without external dependencies
     */
    @SuppressWarnings("deprecation")
    public class OAuthService {

        // Ports to try for OAuth callback server (8080, 8081, 8082, etc.)
        private static final int[] OAUTH_PORTS = {8080, 8081, 8082, 8083, 8084};

        /**
         * Find an available port for OAuth callback server
         * Tries ports 8080, 8081, 8082, etc. until one is available
         */
        private static int findAvailablePort() {
            for (int port : OAUTH_PORTS) {
                try {
                    java.net.ServerSocket testSocket = new java.net.ServerSocket(port);
                    testSocket.close();
                    System.out.println("Using port " + port + " for OAuth callback");
                    return port;
                } catch (java.net.BindException e) {
                    // Port is busy, try next one
                    System.out.println("Port " + port + " is busy, trying next port...");
                } catch (IOException e) {
                    // Other error, try next port
                    System.out.println("Error testing port " + port + ": " + e.getMessage());
                }
            }

            // If we get here, no ports are available
            throw new RuntimeException("No available ports found for OAuth callback server (tried ports: " +
                java.util.Arrays.toString(OAUTH_PORTS) + ")");
        }
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;

    public OAuthService() {
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClients.createDefault();
    }

    /**
     * Performs REAL Google OAuth sign-in
     */
    public User signInWithGoogle() throws Exception {
        return performGoogleOAuth();
    }

    /**
     * Performs REAL GitHub OAuth sign-in
     */
    public User signInWithGitHub() throws Exception {
        return performGitHubOAuth();
    }

    /**
     * Performs OAuth signup with Google (more restrictive)
     */
    public User signupWithGoogle() throws Exception {
        return performOAuthSignup("google");
    }

    /**
     * Performs OAuth signup with GitHub (more restrictive)
     */
    public User signupWithGitHub() throws Exception {
        return performOAuthSignup("github");
    }

    /**
     * Unified OAuth flow - automatically handles login or signup
     */
    public User continueWithGoogle() throws Exception {
        return performUnifiedOAuth("google");
    }

    /**
     * Unified OAuth flow - automatically handles login or signup
     */
    public User continueWithGitHub() throws Exception {
        return performUnifiedOAuth("github");
    }

    /**
     * Performs unified OAuth flow (handles both login and signup automatically)
     */
    private User performUnifiedOAuth(String provider) throws Exception {
        // Check if OAuth credentials are configured
        if (("google".equals(provider) && Constants.OAuth.GOOGLE_CLIENT_ID.startsWith("your-")) ||
            ("github".equals(provider) && Constants.OAuth.GITHUB_CLIENT_ID.startsWith("your-"))) {
            throw new Exception(provider.substring(0, 1).toUpperCase() + provider.substring(1) + " OAuth not configured.");
        }

        // Find an available port for the callback server
        int port = findAvailablePort();

        // Start local callback server on available port
        OAuthCallbackServer callbackServer = new OAuthCallbackServer(provider, port);
        callbackServer.start();

        try {
            // Generate OAuth URL with dynamic port
            String authUrl = "google".equals(provider) ? buildGoogleAuthUrl(port) : buildGitHubAuthUrl(port);
            System.out.println("Opening " + provider + " OAuth URL: " + authUrl);

            // Open browser for user authentication
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                throw new RuntimeException("Desktop browsing not supported");
            }

            // Wait for OAuth callback
            String authCode = callbackServer.waitForAuthCode(300); // 5 minute timeout

            if (authCode == null) {
                throw new Exception("OAuth authentication was cancelled or timed out");
            }

            // Exchange code for tokens and get/create user account
            User user = "google".equals(provider) ?
                exchangeGoogleCodeForUser(authCode, port) :
                exchangeGitHubCodeForUser(authCode, port);

            // Keep server running briefly to serve success page, then stop
            new Thread(() -> {
                try {
                    Thread.sleep(3000); // Keep server alive for 3 seconds
                } catch (InterruptedException e) {
                    // Ignore
                }
                callbackServer.stop();
            }).start();

            return user;

        } catch (Exception e) {
            callbackServer.stop();
            throw e;
        }
    }

    /**
     * Performs complete Google OAuth flow
     */
    private User performGoogleOAuth() throws Exception {
        // Check if OAuth credentials are configured
        if (Constants.OAuth.GOOGLE_CLIENT_ID.startsWith("your-")) {
            throw new Exception("Google OAuth not configured. Please set up Google OAuth credentials in Constants.java");
        }

        // Find an available port for the callback server
        int port = findAvailablePort();

        // Start local callback server on available port
        OAuthCallbackServer callbackServer = new OAuthCallbackServer("google", port);
        callbackServer.start();

        try {
            // Generate OAuth URL with dynamic port
            String authUrl = buildGoogleAuthUrl(port);
            System.out.println("Opening Google OAuth URL: " + authUrl);

            // Open browser for user authentication
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                throw new RuntimeException("Desktop browsing not supported");
            }

            // Wait for OAuth callback
            String authCode = callbackServer.waitForAuthCode(300); // 5 minute timeout

            if (authCode == null) {
                throw new Exception("OAuth authentication was cancelled or timed out");
            }

            // Exchange code for tokens and get user info
            return exchangeGoogleCodeForUser(authCode, port);

        } finally {
            callbackServer.stop();
        }
    }

    /**
     * Performs OAuth signup flow (more restrictive than login)
     */
    private User performOAuthSignup(String provider) throws Exception {
        // Check if OAuth credentials are configured
        if (("google".equals(provider) && Constants.OAuth.GOOGLE_CLIENT_ID.startsWith("your-")) ||
            ("github".equals(provider) && Constants.OAuth.GITHUB_CLIENT_ID.startsWith("your-"))) {
            throw new Exception(provider.substring(0, 1).toUpperCase() + provider.substring(1) + " OAuth not configured.");
        }

        // Find an available port for the callback server
        int port = findAvailablePort();

        // Start local callback server on available port
        OAuthCallbackServer callbackServer = new OAuthCallbackServer(provider, port);
        callbackServer.start();

        try {
            // Generate OAuth URL with dynamic port
            String authUrl = "google".equals(provider) ? buildGoogleAuthUrl(port) : buildGitHubAuthUrl(port);
            System.out.println("Opening " + provider + " OAuth URL: " + authUrl);

            // Open browser for user authentication
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                throw new RuntimeException("Desktop browsing not supported");
            }

            // Wait for OAuth callback
            String authCode = callbackServer.waitForAuthCode(300); // 5 minute timeout

            if (authCode == null) {
                throw new Exception("OAuth authentication was cancelled or timed out");
            }

            // Exchange code for user info and create account (signup mode)
            return "google".equals(provider) ?
                signupWithGoogleCode(authCode, port) :
                signupWithGitHubCode(authCode, port);

        } finally {
            callbackServer.stop();
        }
    }

    /**
     * Performs complete GitHub OAuth flow
     */
    private User performGitHubOAuth() throws Exception {
        // Check if OAuth credentials are configured
        if (Constants.OAuth.GITHUB_CLIENT_ID.startsWith("your-")) {
            throw new Exception("GitHub OAuth not configured. Please set up GitHub OAuth credentials in Constants.java");
        }

        // Find an available port for the callback server
        int port = findAvailablePort();

        // Start local callback server on available port
        OAuthCallbackServer callbackServer = new OAuthCallbackServer("github", port);
        callbackServer.start();

        try {
            // Generate OAuth URL with dynamic port
            String authUrl = buildGitHubAuthUrl(port);
            System.out.println("Opening GitHub OAuth URL: " + authUrl);

            // Open browser for user authentication
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                throw new RuntimeException("Desktop browsing not supported");
            }

            // Wait for OAuth callback
            String authCode = callbackServer.waitForAuthCode(300);

            if (authCode == null) {
                throw new Exception("OAuth authentication was cancelled or timed out");
            }

            // Exchange code for tokens and get user info
            return exchangeGitHubCodeForUser(authCode, port);

        } finally {
            callbackServer.stop();
        }
    }

    /**
     * Builds Google OAuth authorization URL
     */
    private String buildGoogleAuthUrl(int port) {
        String state = "google_oauth_" + UUID.randomUUID().toString();

        try {
            return "https://accounts.google.com/o/oauth2/v2/auth?" +
                   "client_id=" + URLEncoder.encode(Constants.OAuth.GOOGLE_CLIENT_ID, StandardCharsets.UTF_8) + "&" +
                   "redirect_uri=" + URLEncoder.encode("http://localhost:" + port + "/oauth/callback", StandardCharsets.UTF_8) + "&" +
                   "scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8) + "&" +
                   "response_type=code&" +
                   "state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) + "&" +
                   "access_type=offline&" +
                   "prompt=consent";
        } catch (Exception e) {
            // Fallback without encoding if encoding fails
            return "https://accounts.google.com/o/oauth2/v2/auth?" +
                   "client_id=" + Constants.OAuth.GOOGLE_CLIENT_ID + "&" +
                   "redirect_uri=http://localhost:" + port + "/oauth/callback&" +
                   "scope=openid email profile&" +
                   "response_type=code&" +
                   "state=" + state + "&" +
                   "access_type=offline&" +
                   "prompt=consent";
        }
    }

    /**
     * Builds GitHub OAuth authorization URL
     */
    private String buildGitHubAuthUrl(int port) {
        String state = "github_oauth_" + UUID.randomUUID().toString();

        try {
            return "https://github.com/login/oauth/authorize?" +
                   "client_id=" + URLEncoder.encode(Constants.OAuth.GITHUB_CLIENT_ID, StandardCharsets.UTF_8) + "&" +
                   "redirect_uri=" + URLEncoder.encode("http://localhost:" + port + "/oauth/callback", StandardCharsets.UTF_8) + "&" +
                   "scope=" + URLEncoder.encode("user:email", StandardCharsets.UTF_8) + "&" +
                   "state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Fallback without encoding if encoding fails
            return "https://github.com/login/oauth/authorize?" +
                   "client_id=" + Constants.OAuth.GITHUB_CLIENT_ID + "&" +
                   "redirect_uri=http://localhost:" + port + "/oauth/callback&" +
                   "scope=user:email&" +
                   "state=" + state;
        }
    }

    /**
     * Exchanges Google authorization code for user info
     */
    private User exchangeGoogleCodeForUser(String authCode, int port) throws Exception {
        // Exchange code for access token
        String accessToken = exchangeGoogleCodeForToken(authCode, port);

        // Get user info from Google
        JsonNode userInfo = getGoogleUserInfo(accessToken);

        // Extract user details
        String googleId = userInfo.get("id").asText();
        String email = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();

        // Create or find user
        return createOrFindOAuthUser("google", email, name);
    }

    /**
     * Exchanges GitHub authorization code for user info
     */
    private User exchangeGitHubCodeForUser(String authCode, int port) throws Exception {
        // Exchange code for access token
        String accessToken = exchangeGitHubCodeForToken(authCode, port);

        // Get user info from GitHub
        JsonNode userInfo = getGitHubUserInfo(accessToken);
        String email = getGitHubUserEmail(accessToken);

        // Extract user details
        String githubId = userInfo.get("id").asText();

        // Use email prefix as name (same as regular signup)
        String name = email.split("@")[0];

        // Create or find user
        return createOrFindOAuthUser("github", email, name);
    }

    /**
     * Exchanges authorization code for Google access token
     */
    private String exchangeGoogleCodeForToken(String authCode, int port) throws Exception {
        HttpPost post = new HttpPost("https://oauth2.googleapis.com/token");

        Map<String, String> params = new HashMap<>();
        params.put("client_id", Constants.OAuth.GOOGLE_CLIENT_ID);
        params.put("client_secret", Constants.OAuth.GOOGLE_CLIENT_SECRET);
        params.put("code", authCode);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", "http://localhost:" + port + "/oauth/callback");

        String jsonBody = objectMapper.writeValueAsString(params);
        post.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        post.setHeader("Content-Type", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            if (jsonResponse.has("error")) {
                throw new Exception("Google OAuth error: " + jsonResponse.get("error").get("message").asText());
            }

            return jsonResponse.get("access_token").asText();
        }
    }

    /**
     * Exchanges authorization code for GitHub access token
     */
    private String exchangeGitHubCodeForToken(String authCode, int port) throws Exception {
        HttpPost post = new HttpPost("https://github.com/login/oauth/access_token");

        Map<String, String> params = new HashMap<>();
        params.put("client_id", Constants.OAuth.GITHUB_CLIENT_ID);
        params.put("client_secret", Constants.OAuth.GITHUB_CLIENT_SECRET);
        params.put("code", authCode);
        params.put("redirect_uri", "http://localhost:" + port + "/oauth/callback");

        String jsonBody = objectMapper.writeValueAsString(params);
        post.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        post.setHeader("Content-Type", "application/json");
        post.setHeader("Accept", "application/json");

        try (CloseableHttpResponse response = httpClient.execute(post)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonNode jsonResponse = objectMapper.readTree(responseBody);

            if (jsonResponse.has("error")) {
                throw new Exception("GitHub OAuth error: " + jsonResponse.get("error").asText());
            }

            return jsonResponse.get("access_token").asText();
        }
    }

    /**
     * Gets user info from Google API
     */
    private JsonNode getGoogleUserInfo(String accessToken) throws Exception {
        HttpGet get = new HttpGet("https://www.googleapis.com/oauth2/v2/userinfo");
        get.setHeader("Authorization", "Bearer " + accessToken);

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Gets user info from GitHub API
     */
    private JsonNode getGitHubUserInfo(String accessToken) throws Exception {
        HttpGet get = new HttpGet("https://api.github.com/user");
        get.setHeader("Authorization", "Bearer " + accessToken);
        get.setHeader("Accept", "application/vnd.github.v3+json");

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            return objectMapper.readTree(responseBody);
        }
    }

    /**
     * Gets user email from GitHub API
     */
    private String getGitHubUserEmail(String accessToken) throws Exception {
        HttpGet get = new HttpGet("https://api.github.com/user/emails");
        get.setHeader("Authorization", "Bearer " + accessToken);
        get.setHeader("Accept", "application/vnd.github.v3+json");

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            JsonNode emails = objectMapper.readTree(responseBody);

            // Return primary email
            for (JsonNode email : emails) {
                if (email.get("primary").asBoolean()) {
                    return email.get("email").asText();
                }
            }

            // Fallback to first email
            if (emails.size() > 0) {
                return emails.get(0).get("email").asText();
            }

            throw new Exception("No email found for GitHub user");
        }
    }

    /**
     * Handles OAuth signup with Google (strict - no account linking)
     */
    private User signupWithGoogleCode(String authCode, int port) throws Exception {
        // Exchange code for access token
        String accessToken = exchangeGoogleCodeForToken(authCode, port);

        // Get user info from Google
        JsonNode userInfo = getGoogleUserInfo(accessToken);

        // Extract user details
        String googleId = userInfo.get("id").asText();
        String email = userInfo.get("email").asText();
        String name = userInfo.get("name").asText();

        // Use unified OAuth user creation (strict mode for signup)
        return createOrFindOAuthUser("google", email, name);
    }

    /**
     * Handles OAuth signup with GitHub (strict - no account linking)
     */
    private User signupWithGitHubCode(String authCode, int port) throws Exception {
        // Exchange code for access token
        String accessToken = exchangeGitHubCodeForToken(authCode, port);

        // Get user info from GitHub
        JsonNode userInfo = getGitHubUserInfo(accessToken);
        String email = getGitHubUserEmail(accessToken);

        // Extract user details
        String githubId = userInfo.get("id").asText();

        // Use email prefix as name (same as regular signup)
        String name = email.split("@")[0];

        // Use unified OAuth user creation (strict mode for signup)
        return createOrFindOAuthUser("github", email, name);
    }

    /**
     * Creates or finds user in database (used for login - allows linking)
     */
    private User createOrFindOAuthUser(String provider, String email, String name) {
        UserDAO userDAO = new UserDAO();

        // Try to find existing OAuth user by email and provider
        User existingUser = userDAO.findByOAuthEmail(provider, email);
        if (existingUser != null) {
            return existingUser;
        }

        // Check if email is already used by a regular user
        User regularUser = userDAO.findByEmail(email);
        if (regularUser != null) {
            throw new RuntimeException("An account with this email already exists. Please use regular login instead.");
        }

        // Create new OAuth user
        String finalName = name != null ? name : email.split("@")[0];
        if (userDAO.saveOAuthUser(provider, email, finalName)) {
            return userDAO.findByOAuthEmail(provider, email);
        }

        throw new RuntimeException("Failed to create OAuth account");
    }

    /**
     * Local HTTP server to handle OAuth callbacks
     */
    private static class OAuthCallbackServer {
        private HttpServer server;
        private CompletableFuture<String> authCodeFuture;
        private String provider;
        private int port;

        public OAuthCallbackServer(String provider, int port) {
            this.provider = provider;
            this.port = port;
            this.authCodeFuture = new CompletableFuture<>();
        }

        public void start() throws IOException {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/oauth/callback", new OAuthCallbackHandler());
            server.setExecutor(null);
            server.start();
        }

        public void stop() {
            if (server != null) {
                server.stop(0);
            }
        }

        public String waitForAuthCode(long timeoutSeconds) throws Exception {
            try {
                return authCodeFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                return null; // Timeout or cancellation
            }
        }

        private class OAuthCallbackHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String query = exchange.getRequestURI().getQuery();

                String response = "";
                int statusCode = 200;

                try {
                    if (query != null && query.contains("code=")) {
                        String authCode = extractParameter(query, "code");
                        authCodeFuture.complete(authCode);

                        response = generateSuccessHtml();
                    } else if (query != null && query.contains("error=")) {
                        authCodeFuture.complete(null); // Signal cancellation/error
                        response = generateErrorHtml();
                        statusCode = 400;
                    } else {
                        authCodeFuture.complete(null);
                        response = generateErrorHtml();
                        statusCode = 400;
                    }
                } catch (Exception e) {
                    authCodeFuture.complete(null);
                    response = generateErrorHtml();
                    statusCode = 500;
                }

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(statusCode, response.length());

                try (var os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }

            private String extractParameter(String query, String paramName) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith(paramName + "=")) {
                        return param.substring(paramName.length() + 1);
                    }
                }
                return null;
            }

            private String generateSuccessHtml() {
                return "<!DOCTYPE html><html><head><title>Authentication Successful</title>" +
                       "<meta http-equiv='refresh' content='1; url=about:blank'>" +
                       "<style>body{font-family:Arial;text-align:center;padding:50px;background:#fff;}" +
                       "h1{color:#28a745;}</style>" +
                       "</head><body>" +
                       "<h1>✓ Authentication Successful!</h1>" +
                       "<p>Redirecting...</p>" +
                       "</body></html>";
            }

            private String generateErrorHtml() {
                return "<!DOCTYPE html><html><head><title>Error</title></head><body>" +
                       "<h1>✗ Authentication Failed</h1>" +
                       "<p>You can close this window and try again.</p>" +
                       "</body></html>";
            }
        }
    }
}
