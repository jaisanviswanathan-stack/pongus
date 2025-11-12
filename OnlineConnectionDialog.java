import javax.swing.*;
import java.awt.*;

/**
 * User-friendly dialog for setting up online multiplayer connections
 * Handles ngrok, Tailscale, and direct IP connections
 */
public class OnlineConnectionDialog extends JDialog {

    private int result = JOptionPane.CANCEL_OPTION;
    private String selectedAddress = null;
    private int selectedMethod = -1;

    public OnlineConnectionDialog(JFrame parent) {
        super(parent, "Online Multiplayer Setup", true);
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Host game
        tabbedPane.addTab("Host Game", createHostPanel());

        // Tab 2: Join game
        tabbedPane.addTab("Join Game", createJoinPanel());

        // Tab 3: Instructions
        tabbedPane.addTab("Instructions", createInstructionsPanel());

        add(tabbedPane);
    }

    private JPanel createHostPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Host a Game - Choose Connection Method");
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Option 1: ngrok (recommended)
        JButton ngrokBtn = new JButton("🚀 Use ngrok (No Port Forwarding)");
        ngrokBtn.setFont(ngrokBtn.getFont().deriveFont(12f));
        ngrokBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        ngrokBtn.addActionListener(e -> hostWithNgrok());
        optionsPanel.add(ngrokBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(new JLabel("  Best for: Cross-country play, easiest setup"));
        optionsPanel.add(new JLabel("  Requires: Free ngrok account + installation"));
        optionsPanel.add(Box.createVerticalStrut(15));

        // Option 2: Tailscale
        JButton tailscaleBtn = new JButton("🔒 Use Tailscale (Secure VPN)");
        tailscaleBtn.setFont(tailscaleBtn.getFont().deriveFont(12f));
        tailscaleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        tailscaleBtn.addActionListener(e -> hostWithTailscale());
        optionsPanel.add(tailscaleBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(new JLabel("  Best for: Private mesh network, most secure"));
        optionsPanel.add(new JLabel("  Requires: Free Tailscale account + installation"));
        optionsPanel.add(Box.createVerticalStrut(15));

        // Option 3: Direct IP
        JButton directBtn = new JButton("⚙️ Use Direct IP (Port Forwarding)");
        directBtn.setFont(directBtn.getFont().deriveFont(12f));
        directBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        directBtn.addActionListener(e -> hostWithDirectIP());
        optionsPanel.add(directBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(new JLabel("  Best for: Same home network"));
        optionsPanel.add(new JLabel("  Requires: Manual router port forwarding"));

        panel.add(optionsPanel, BorderLayout.CENTER);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        panel.add(cancelBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createJoinPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Join a Game - Enter Host Address");
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        inputPanel.add(new JLabel("Enter the address your friend shared:"));
        inputPanel.add(Box.createVerticalStrut(5));

        JTextField addressField = new JTextField();
        addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        inputPanel.add(addressField);

        inputPanel.add(Box.createVerticalStrut(10));
        inputPanel.add(new JLabel("Examples:"));
        inputPanel.add(new JLabel("  • abc123.ngrok.io  (ngrok)"));
        inputPanel.add(new JLabel("  • 100.64.5.123  (Tailscale)"));
        inputPanel.add(new JLabel("  • 192.168.1.50  (Direct IP)"));

        panel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        JButton connectBtn = new JButton("Connect");
        connectBtn.addActionListener(e -> {
            selectedAddress = addressField.getText().trim();
            if (!selectedAddress.isEmpty()) {
                result = JOptionPane.OK_OPTION;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter an address", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(cancelBtn);
        buttonPanel.add(connectBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createInstructionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea instructionsArea = new JTextArea();
        instructionsArea.setEditable(false);
        instructionsArea.setLineWrap(true);
        instructionsArea.setWrapStyleWord(true);
        instructionsArea.setText(
            "QUICK START GUIDE\n\n" +
            "METHOD 1: ngrok (Recommended)\n" +
            "1. Download ngrok from https://ngrok.com/download\n" +
            "2. Sign up for free account\n" +
            "3. Extract and run: ngrok http 12345\n" +
            "4. Share the URL with your friend\n\n" +
            "METHOD 2: Tailscale (Most Secure)\n" +
            "1. Download Tailscale from https://tailscale.com/download\n" +
            "2. Install and sign in\n" +
            "3. Your Tailscale IP will be shown\n" +
            "4. Share your Tailscale IP with your friend\n\n" +
            "METHOD 3: Direct IP (Port Forwarding)\n" +
            "1. Open your router settings (192.168.1.1)\n" +
            "2. Find Port Forwarding in Advanced settings\n" +
            "3. Forward port 12345 to your PC's local IP\n" +
            "4. Share your public IP with your friend\n\n" +
            "WHY NO PORT FORWARDING?\n" +
            "ngrok and Tailscale bypass the need for manual port forwarding,\n" +
            "making it much easier to play with friends across different networks!"
        );

        JScrollPane scrollPane = new JScrollPane(instructionsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        panel.add(closeBtn, BorderLayout.SOUTH);

        return panel;
    }

    private void hostWithNgrok() {
        JOptionPane.showMessageDialog(this,
            "Before hosting, please:\n\n" +
            "1. Download ngrok from https://ngrok.com/download\n" +
            "2. Sign up for a free account\n" +
            "3. Open terminal/command prompt\n" +
            "4. Run: ngrok http 12345\n\n" +
            "Click OK when ready!",
            "Setup ngrok",
            JOptionPane.INFORMATION_MESSAGE);

        result = JOptionPane.OK_OPTION;
        selectedMethod = OnlineMultiplayerManager.METHOD_NGROK;
        dispose();
    }

    private void hostWithTailscale() {
        JOptionPane.showMessageDialog(this,
            "Before hosting, please:\n\n" +
            "1. Download Tailscale from https://tailscale.com/download\n" +
            "2. Install and sign in\n" +
            "3. Your Tailscale IP will be automatically assigned\n\n" +
            "Click OK when ready!",
            "Setup Tailscale",
            JOptionPane.INFORMATION_MESSAGE);

        result = JOptionPane.OK_OPTION;
        selectedMethod = OnlineMultiplayerManager.METHOD_TAILSCALE;
        dispose();
    }

    private void hostWithDirectIP() {
        JOptionPane.showMessageDialog(this,
            "To host with port forwarding:\n\n" +
            "1. Open your router's admin page (192.168.1.1 or 192.168.0.1)\n" +
            "2. Find Port Forwarding settings\n" +
            "3. Forward port 12345 to your computer's local IP\n" +
            "4. Save and restart your router\n\n" +
            "Click OK when ready!",
            "Setup Port Forwarding",
            JOptionPane.INFORMATION_MESSAGE);

        result = JOptionPane.OK_OPTION;
        selectedMethod = OnlineMultiplayerManager.METHOD_DIRECT;
        dispose();
    }

    public int getResult() {
        return result;
    }

    public String getSelectedAddress() {
        return selectedAddress;
    }

    public int getSelectedMethod() {
        return selectedMethod;
    }
}
