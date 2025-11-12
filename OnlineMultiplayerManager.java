import java.io.*;
import java.net.*;

/**
 * Manages online multiplayer connections without requiring port forwarding
 * Supports multiple connection methods:
 * 1. ngrok relay (automatic)
 * 2. Tailscale VPN (secure mesh network)
 * 3. Direct IP (fallback to port forwarding)
 */
public class OnlineMultiplayerManager {

    // Connection method options
    public static final int METHOD_NGROK = 1;
    public static final int METHOD_TAILSCALE = 2;
    public static final int METHOD_DIRECT = 3;

    private int connectionMethod = METHOD_NGROK; // Default
    private String publicUrl = null;
    private boolean isConnected = false;

    /**
     * Start hosting a game using ngrok (no port forwarding needed)
     * Returns the public URL to share with your friend
     */
    public String startHostingWithNgrok() throws Exception {
        System.out.println("Starting ngrok tunnel...");

        // Check if ngrok is installed
        if (!isNgrokInstalled()) {
            throw new Exception(
                "ngrok not found. Please install it:\n" +
                "1. Download from https://ngrok.com/download\n" +
                "2. Extract to a folder\n" +
                "3. Run: ngrok http 12345\n" +
                "4. Share the URL with your friend"
            );
        }

        try {
            // Start ngrok tunnel in background
            ProcessBuilder pb = new ProcessBuilder("ngrok", "http", "12345", "--log=stdout");
            Process process = pb.start();

            // Read ngrok output to find the public URL
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            long startTime = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTime) < 10000) { // 10 second timeout
                if ((line = reader.readLine()) != null) {
                    // Look for the forwarding URL in ngrok output
                    if (line.contains("Forwarding") && line.contains("->")) {
                        String[] parts = line.split("Forwarding")[1].split("->");
                        if (parts.length > 0) {
                            String url = parts[0].trim();
                            // Extract domain from URL (e.g., "https://abc123.ngrok.io")
                            if (url.contains("https://")) {
                                url = url.replace("https://", "").replace("http://", "");
                                this.publicUrl = url;
                                this.connectionMethod = METHOD_NGROK;
                                System.out.println("✓ ngrok tunnel started!");
                                System.out.println("✓ Public URL: " + url);
                                return url;
                            }
                        }
                    }
                }
            }

            throw new Exception("Could not extract ngrok URL. Please try again.");

        } catch (IOException e) {
            throw new Exception("Failed to start ngrok: " + e.getMessage());
        }
    }

    /**
     * Start hosting with Tailscale (creates secure mesh network)
     * More secure alternative to ngrok
     */
    public String startHostingWithTailscale() throws Exception {
        System.out.println("Starting Tailscale connection...");

        if (!isTailscaleInstalled()) {
            throw new Exception(
                "Tailscale not found. Please install it:\n" +
                "1. Download from https://tailscale.com/download\n" +
                "2. Install and sign in\n" +
                "3. Your machine will get a unique Tailscale IP"
            );
        }

        try {
            // Get your Tailscale IP
            ProcessBuilder pb = new ProcessBuilder("tailscale", "ip");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String tailscaleIp = reader.readLine();

            if (tailscaleIp != null && !tailscaleIp.isEmpty()) {
                this.publicUrl = tailscaleIp;
                this.connectionMethod = METHOD_TAILSCALE;
                System.out.println("✓ Tailscale connection ready!");
                System.out.println("✓ Your Tailscale IP: " + tailscaleIp);
                return tailscaleIp;
            }

            throw new Exception("Could not get Tailscale IP. Make sure you're logged in.");

        } catch (IOException e) {
            throw new Exception("Failed to get Tailscale IP: " + e.getMessage());
        }
    }

    /**
     * Connect to a host using one of the connection methods
     */
    public Socket connectToHost(String hostAddress) throws IOException {
        System.out.println("Attempting to connect to: " + hostAddress);

        // Try different connection methods
        Socket socket = null;
        IOException lastError = null;

        // Method 1: Try as ngrok URL
        try {
            socket = connectToNgrokHost(hostAddress);
            if (socket != null) {
                this.publicUrl = hostAddress;
                this.connectionMethod = METHOD_NGROK;
                System.out.println("✓ Connected via ngrok!");
                return socket;
            }
        } catch (IOException e) {
            lastError = e;
            System.out.println("✗ ngrok connection failed: " + e.getMessage());
        }

        // Method 2: Try as Tailscale IP
        try {
            socket = new Socket(hostAddress, 12345);
            this.publicUrl = hostAddress;
            this.connectionMethod = METHOD_TAILSCALE;
            System.out.println("✓ Connected via Tailscale!");
            return socket;
        } catch (IOException e) {
            lastError = e;
            System.out.println("✗ Tailscale connection failed: " + e.getMessage());
        }

        // Method 3: Try as direct IP (port forwarding)
        try {
            socket = new Socket(hostAddress, 12345);
            this.publicUrl = hostAddress;
            this.connectionMethod = METHOD_DIRECT;
            System.out.println("✓ Connected via direct IP!");
            return socket;
        } catch (IOException e) {
            lastError = e;
            System.out.println("✗ Direct IP connection failed: " + e.getMessage());
        }

        // All methods failed
        throw new IOException(
            "Could not connect to host.\n" +
            "Tried:\n" +
            "- ngrok URL (https://...)\n" +
            "- Tailscale IP\n" +
            "- Direct IP\n\n" +
            "Last error: " + (lastError != null ? lastError.getMessage() : "Unknown")
        );
    }

    /**
     * Connect to a host through ngrok
     * ngrok provides HTTP forwarding, so we need to handle the HTTP upgrade
     */
    private Socket connectToNgrokHost(String ngrokUrl) throws IOException {
        // Remove protocol if included
        String host = ngrokUrl.replace("https://", "").replace("http://", "");

        try {
            // Create HTTPS connection to ngrok
            Socket socket = new Socket(host, 443);
            return socket;
        } catch (IOException e) {
            // Fallback to HTTP
            try {
                Socket socket = new Socket(host, 80);
                return socket;
            } catch (IOException e2) {
                throw e;
            }
        }
    }

    /**
     * Check if ngrok is installed on the system
     */
    private boolean isNgrokInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("ngrok", "--version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if Tailscale is installed on the system
     */
    private boolean isTailscaleInstalled() {
        try {
            ProcessBuilder pb = new ProcessBuilder("tailscale", "version");
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Getters
    public String getPublicUrl() { return publicUrl; }
    public int getConnectionMethod() { return connectionMethod; }
    public String getConnectionMethodName() {
        switch(connectionMethod) {
            case METHOD_NGROK: return "ngrok relay";
            case METHOD_TAILSCALE: return "Tailscale VPN";
            case METHOD_DIRECT: return "Direct IP";
            default: return "Unknown";
        }
    }
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
}
