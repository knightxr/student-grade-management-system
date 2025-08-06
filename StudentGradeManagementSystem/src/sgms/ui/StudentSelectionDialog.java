package sgms.ui;

import sgms.dao.StudentDAO;
import sgms.model.Student;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dialog that allows selecting students to enroll in a course.
 */
public class StudentSelectionDialog extends JDialog {

    private final StudentDAO dao;
    private final int courseId;
    private StudentSelectionTableModel model;
    private JTable table;
    private Set<Integer> originallyEnrolled;

    public StudentSelectionDialog(Frame owner, StudentDAO dao, int courseId) throws Exception {
        super(owner, "Select Students", true);
        this.dao = dao;
        this.courseId = courseId;
        buildUI();
        loadStudents();
        setSize(500, 400);
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        table = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (model != null) {
                    if (model.isNewlySelected(row)) {
                        c.setBackground(Color.GREEN);
                    } else if (model.isDeselected(row)) {
                        c.setBackground(Color.RED);
                    } else {
                        c.setBackground(isRowSelected(row) ? getSelectionBackground() : Color.WHITE);
                    }
                }
                return c;
            }
        };
        JScrollPane scroll = new JScrollPane(table);
        JButton save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        save.addActionListener(this::onSave);
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(save);
        buttons.add(cancel);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scroll, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private void loadStudents() throws Exception {
        List<Student> all = dao.findAll();
        List<Student> enrolled = dao.findByCourse(courseId);
        originallyEnrolled = new HashSet<>();
        for (Student s : enrolled) {
            originallyEnrolled.add(s.getStudentId());
        }
        model = new StudentSelectionTableModel(all, originallyEnrolled);
        table.setModel(model);
        table.repaint();
    }

    private void onSave(ActionEvent evt) {
        try {
            Set<Integer> selected = model.getSelectedStudentIds();
            for (Integer id : selected) {
                if (!originallyEnrolled.contains(id)) {
                    dao.enrollStudentInCourse(id, courseId);
                }
            }
            for (Integer id : originallyEnrolled) {
                if (!selected.contains(id)) {
                    dao.removeStudentFromCourse(id, courseId);
                }
            }
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to update enrollments: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}