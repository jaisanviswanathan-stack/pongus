import javax.swing.*;
import java.awt.*;

/**
 * Dialog for selecting game mode (Local vs Online)
 */
public class GameModeSelector extends JDialog {

    public static final int MODE_LOCAL = 1;
    public static final int MODE_HOST = 2;
    public static final int MODE_JOIN = 3;
    public static final int MODE_CANCEL = 0;

    private int selectedMode = MODE_CANCEL;
    private String hostAddress = null;
    private int port = 12345;

    public GameModeSelector(JFrame parent) {
        super(parent, "Pong Game Mode", true);
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Choose Game Mode");
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Options panel
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Option 1: Local Game
        JButton localBtn = new JButton("Local Game (2 Players on Same PC)");
        localBtn.setFont(localBtn.getFont().deriveFont(13f));
        localBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        localBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        localBtn.addActionListener(e -> selectMode(MODE_LOCAL));
        optionsPanel.add(localBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(createDescriptionLabel("Play with a friend on the same computer"));
        optionsPanel.add(Box.createVerticalStrut(15));

        // Option 2: Host Online Game
        JButton hostBtn = new JButton("Host Online Game");
        hostBtn.setFont(hostBtn.getFont().deriveFont(13f));
        hostBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        hostBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        hostBtn.addActionListener(e -> selectMode(MODE_HOST));
        optionsPanel.add(hostBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(createDescriptionLabel("Start hosting a game - share your link with a friend"));
        optionsPanel.add(Box.createVerticalStrut(15));

        // Option 3: Join Online Game
        JButton joinBtn = new JButton("Join Online Game");
        joinBtn.setFont(joinBtn.getFont().deriveFont(13f));
        joinBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        joinBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        joinBtn.addActionListener(e -> selectMode(MODE_JOIN));
        optionsPanel.add(joinBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(createDescriptionLabel("Connect to a friend's hosted game"));
        optionsPanel.add(Box.createVerticalStrut(15));

        // Option 4: AI Game
        JButton aiBtn = new JButton("Play Against AI");
        aiBtn.setFont(aiBtn.getFont().deriveFont(13f));
        aiBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        aiBtn.addActionListener(e -> selectMode(MODE_LOCAL)); // Same as local for now
        optionsPanel.add(aiBtn);
        optionsPanel.add(Box.createVerticalStrut(5));
        optionsPanel.add(createDescriptionLabel("Play against the AI opponent"));

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> {
            selectedMode = MODE_CANCEL;
            dispose();
        });
        buttonPanel.add(cancelBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createDescriptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(11f));
        label.setForeground(Color.GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void selectMode(int mode) {
        if (mode == MODE_HOST) {
            // Show host dialog
            showHostDialog();
        } else if (mode == MODE_JOIN) {
            // Show join dialog
            showJoinDialog();
        } else {
            selectedMode = mode;
            dispose();
        }
    }

    private void showHostDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Starting host server..."));
        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Port: "));
        JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(12345, 1024, 65535, 1));
        portSpinner.setMaximumSize(new Dimension(100, 30));
        panel.add(portSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Host Game", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            port = (int) portSpinner.getValue();
            selectedMode = MODE_HOST;
            dispose();
        }
    }

    private void showJoinDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel("Enter the host address:"));
        panel.add(Box.createVerticalStrut(5));

        JTextField addressField = new JTextField(20);
        addressField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(addressField);

        panel.add(Box.createVerticalStrut(10));
        panel.add(new JLabel("Examples:"));
        panel.add(new JLabel("  - abc123.ngrok.io (ngrok)"));
        panel.add(new JLabel("  - 100.64.5.123 (Tailscale)"));
        panel.add(new JLabel("  - 192.168.1.100 (Local IP)"));

        int result = JOptionPane.showConfirmDialog(this, panel, "Join Game", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String address = addressField.getText().trim();
            if (!address.isEmpty()) {
                hostAddress = address;
                selectedMode = MODE_JOIN;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please enter an address", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public int getSelectedMode() {
        return selectedMode;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getPort() {
        return port;
    }
}
