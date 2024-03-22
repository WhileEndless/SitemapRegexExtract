package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BurpExtender implements IBurpExtender, ITab {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    private JTextField regexArea;
    private JTextArea resultArea;
    private JButton checkButton;
    private JButton cancelButton;
    private JButton updateButton;
    private JComboBox<String> baseUrlsComboBox;
    private JPanel mainPanel;
    private volatile boolean isCancelled = false;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();
        callbacks.setExtensionName("Sitemap Regex Extract");

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        baseUrlsComboBox = new JComboBox<>();
        baseUrlsComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, baseUrlsComboBox.getPreferredSize().height));
        updateButton = new JButton("Update URLs");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBaseUrlsComboBox();
            }
        });
        updateBaseUrlsComboBox();
        regexArea = new JTextField("Enter regex here...");
        regexArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, regexArea.getPreferredSize().height));
        regexArea.setForeground(Color.GRAY);
        
        regexArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (regexArea.getText().equals("Enter regex here...")) {
                    regexArea.setText("");
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (regexArea.getText().isEmpty()) {
                    regexArea.setText("Enter regex here...");
                }
            }
        });

        resultArea = new JTextArea(18, 30);
        resultArea.setEditable(false);

        checkButton = new JButton("Search");
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCancelled = false;
                checkScope();
            }
        });

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isCancelled = true;
            }
        });
        cancelButton.setEnabled(false);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(baseUrlsComboBox);
        topPanel.add(updateButton);

        mainPanel.add(topPanel);
        mainPanel.add(regexArea);
        mainPanel.add(checkButton);
        mainPanel.add(cancelButton);
        mainPanel.add(new JScrollPane(resultArea));

        callbacks.addSuiteTab(this);
    }

    private void updateBaseUrlsComboBox() {
        SwingUtilities.invokeLater(() -> {
            Object selectedObject = baseUrlsComboBox.getSelectedItem();

            Set<String> baseUrlsSet = new HashSet<>();
            for (IHttpRequestResponse item : callbacks.getSiteMap(null)) {
                URL url = helpers.analyzeRequest(item).getUrl();
                String baseUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() > -1 ? ":" + url.getPort() : "");
                baseUrlsSet.add(baseUrl);
            }

            List<String> baseUrls = new ArrayList<>(baseUrlsSet);
            Collections.sort(baseUrls);

            baseUrls.add(0, "All");

            baseUrlsComboBox.setModel(new DefaultComboBoxModel<>(baseUrls.toArray(new String[0])));

            if (selectedObject != null && baseUrls.contains(selectedObject)) {
                baseUrlsComboBox.setSelectedItem(selectedObject);
            } else {
                baseUrlsComboBox.setSelectedItem("All");
            }
        });
    }


    private void checkScope() {
    	cancelButton.setEnabled(true);
    	checkButton.setEnabled(false);
        SwingUtilities.invokeLater(() -> checkButton.setText("Searching..."));
        new Thread(() -> {
            try {
                String selectedBaseUrl = (String) baseUrlsComboBox.getSelectedItem();
                String userRegex = regexArea.getText().equals("Enter regex here...") ? "" : regexArea.getText().trim();
                Pattern pattern = Pattern.compile(userRegex);

                Set<String> matchedGroups = new HashSet<>();
                StringBuilder matchedRequests = new StringBuilder();

                for (IHttpRequestResponse item : callbacks.getSiteMap(null)) {
                    if (isCancelled) break;
                    URL url = helpers.analyzeRequest(item).getUrl();
                    if (url.toString().startsWith(selectedBaseUrl) || selectedBaseUrl=="All") {
                    	if (item.getResponse() == null) {
                            continue;
                        }
                        String responseString = helpers.bytesToString(item.getResponse());
                        Matcher matcher = pattern.matcher(responseString);
                        while (matcher.find()) {
                            String match = matcher.group(matcher.groupCount());
                            if (matchedGroups.add(match)) {
                                matchedRequests.append(match.trim()).append("\n");
                            }
                        }
                    }
                }

                SwingUtilities.invokeLater(() -> {
                    resultArea.setText(matchedRequests.toString());
                    checkButton.setText("Search");
                });
            } catch (PatternSyntaxException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(mainPanel, "Invalid regex pattern.", "Error", JOptionPane.ERROR_MESSAGE);
                    checkButton.setText("Search");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(mainPanel, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    checkButton.setText("Search");
                });
            }
            cancelButton.setEnabled(false);
        	checkButton.setEnabled(true);
        }).start();
        
    }

    @Override
    public String getTabCaption() {
        return "Sitemap Regex Extract";
    }

    @Override
    public Component getUiComponent() {
        return mainPanel;
    }
}
