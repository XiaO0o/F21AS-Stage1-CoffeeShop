package coffeeshop.simulation.view;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogPanel extends JPanel {

    private final JTextArea logArea = new JTextArea();

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Event Log"));
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);
    }

    public void setLogs(List<String> logs) {
        String text = logs == null ? "" : String.join(System.lineSeparator(), logs);
        logArea.setText(text);
        // Keep the latest event visible.
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
