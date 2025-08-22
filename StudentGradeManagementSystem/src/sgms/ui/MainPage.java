package sgms.ui;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import sgms.dao.DB;
import sgms.dao.AssignmentDAO;
import sgms.dao.GradeDAO;
import sgms.dao.StudentDAO;
import sgms.dao.AttendanceDAO;
import sgms.dao.FeedbackDAO;
import sgms.dao.CourseDAO;
import sgms.dao.impl.UcanaccessAssignmentDAO;
import sgms.dao.impl.UcanaccessGradeDAO;
import sgms.dao.impl.UcanaccessStudentDAO;
import sgms.dao.impl.UcanaccessAttendanceDAO;
import sgms.dao.impl.UcanaccessFeedbackDAO;
import sgms.dao.impl.UcanaccessCourseDAO;
import sgms.model.Assignment;
import sgms.model.Course;
import sgms.model.Student;
import sgms.service.ValidationService;

/**
 *
 * @author Jacques Smit 12E Main application frame that hosts the navigation
 * sidebar and a data table
 */
public class MainPage extends javax.swing.JFrame {

    private final StudentDAO studentDAO = new UcanaccessStudentDAO();
    private final CourseDAO courseDAO = new UcanaccessCourseDAO();
    private StudentTableModel studentTableModel;
    private StudentSelectionTableModel studentSelectionModel;
    private StudentGradesTableModel studentGradesModel;
    private FinalGradesTableModel finalGradesModel;
    private AttendanceTableModel attendanceModel;
    private StudentFeedbackTableModel feedbackModel;
    private CourseTableModel courseModel;
    private AssignmentTableModel assignmentModel;
    // Delete-mode flags + cached column instances (per view)
    private boolean studentDeleteMode = false;
    private javax.swing.table.TableColumn studentDeleteColumn;

    private boolean assignmentDeleteMode = false;
    private javax.swing.table.TableColumn assignmentDeleteColumn;

    private boolean courseDeleteMode = false;
    private javax.swing.table.TableColumn courseDeleteColumn;
    private boolean selectionMode = false;
    private int attendanceTodayColumn = -1;
    private final FeedbackDAO feedbackDAO = new UcanaccessFeedbackDAO();
    private final AssignmentDAO assignmentDAO = new UcanaccessAssignmentDAO();
    private final GradeDAO gradeDAO = new UcanaccessGradeDAO();
    private javax.swing.JButton lastActionButton;

    private void setActiveButton(javax.swing.JButton active) {
        java.awt.Color defaultColor = java.awt.Color.WHITE;
        java.awt.Color activeColor = new java.awt.Color(0, 102, 204);

        javax.swing.JButton[] buttons = {
            jButtonViewStudents,
            jButtonViewStudentGrades,
            jButtonViewFinalGrades,
            jButtonAttendance,
            jButtonStudentFeedback,
            jButtonManageCourses,
            jButtonManageAssignments
        };

        int i;
        for (i = 0; i < buttons.length; i++) {
            javax.swing.JButton b = buttons[i];
            if (b != null) {
                b.setBackground(b == active ? activeColor : defaultColor);
                b.setOpaque(true);
                b.setBorderPainted(false);
            }
        }
    }

    private void installContextTracking() {
        java.awt.event.ActionListener tracker = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Object src = e.getSource();
                if (src instanceof javax.swing.JButton && src != jButtonHelp) {
                    lastActionButton = (javax.swing.JButton) src;
                }
            }
        };

        javax.swing.JButton[] buttons = {
            jButtonViewStudents,
            jButtonViewStudentGrades,
            jButtonViewFinalGrades,
            jButtonAttendance,
            jButtonStudentFeedback,
            jButtonManageCourses,
            jButtonManageAssignments,
            jButtonCreateReportCard,
            jButtonAdd,
            jButtonDelete,
            jButtonSave,
            jButtonEdit,
            jButtonSearch
        };

        int i;
        for (i = 0; i < buttons.length; i++) {
            if (buttons[i] != null) {
                buttons[i].addActionListener(tracker);
            }
        }
    }

    /**
     * Creates the main window and wires up table + search.
     */
    public MainPage() {
        initComponents();
        setResizable(false);

        // Custom table with row colouring for delete/enrol/attendance cues
        jTable = new javax.swing.JTable() {
            @Override
            public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                java.awt.Component c = super.prepareRenderer(renderer, row, column);

                java.awt.Color base = (row % 2 == 0) ? java.awt.Color.WHITE : new java.awt.Color(235, 235, 235);
                boolean colored = false;

                if (selectionMode && MainPage.this.studentSelectionModel != null) {
                    int modelRow = convertRowIndexToModel(row);
                    if (MainPage.this.studentSelectionModel.isNewlySelected(modelRow)) {
                        c.setBackground(java.awt.Color.GREEN);
                        colored = true;
                    } else if (MainPage.this.studentSelectionModel.isDeselected(modelRow)) {
                        c.setBackground(java.awt.Color.RED);
                        colored = true;
                    }
                } else {
                    if (attendanceModel != null && column == attendanceTodayColumn && !isRowSelected(row)) {
                        c.setBackground(new java.awt.Color(255, 255, 200));
                        colored = true;
                    }
                    if (courseModel != null) {
                        int modelRow = convertRowIndexToModel(row);
                        if (courseModel.isMarkedForDeletion(modelRow) && !isRowSelected(row)) {
                            c.setBackground(java.awt.Color.RED);
                            colored = true;
                        }
                    } else if (assignmentModel != null) {
                        int modelRow = convertRowIndexToModel(row);
                        if (assignmentModel.isMarkedForDeletion(modelRow) && !isRowSelected(row)) {
                            c.setBackground(java.awt.Color.RED);
                            colored = true;
                        }
                    } else if (studentTableModel != null && studentDeleteMode) {
                        int modelRow = convertRowIndexToModel(row);
                        if (studentTableModel.isMarkedForDeletion(modelRow) && !isRowSelected(row)) {
                            c.setBackground(java.awt.Color.RED);
                            colored = true;
                        }
                    }
                }

                if (!colored && !isRowSelected(row)) {
                    c.setBackground(base);
                }

                if (c instanceof javax.swing.JComponent) {
                    ((javax.swing.JComponent) c).setOpaque(true);
                }
                return c;
            }
        };

        jScrollPane.setViewportView(jTable);
        jTable.setModel(new javax.swing.table.DefaultTableModel());
        jTable.setAutoCreateRowSorter(true);
        jTable.getTableHeader().setFont(jTable.getTableHeader().getFont().deriveFont(java.awt.Font.BOLD));

        // Simple placeholder for the search box
        jTextFieldSearch.setText("Search");
        jTextFieldSearch.setForeground(java.awt.Color.GRAY);
        jTextFieldSearch.addFocusListener(new java.awt.event.FocusListener() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if ("Search".equals(jTextFieldSearch.getText())) {
                    jTextFieldSearch.setText("");
                    jTextFieldSearch.setForeground(java.awt.Color.BLACK);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (jTextFieldSearch.getText().trim().isEmpty()) {
                    jTextFieldSearch.setText("Search");
                    jTextFieldSearch.setForeground(java.awt.Color.GRAY);
                }
            }
        });

        // Hook up Save/Edit buttons without lambdas
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });

        // Course selection is enabled only when needed
        jComboBox.setEnabled(false);
        jComboBox.removeAllItems();

        setActiveButton(null);

        // Click a comment cell to show full note
        jTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (feedbackModel != null) {
                    int row = jTable.rowAtPoint(evt.getPoint());
                    int col = jTable.columnAtPoint(evt.getPoint());
                    if (row >= 0 && col == 2) {
                        int modelRow = jTable.convertRowIndexToModel(row);
                        String full = feedbackModel.getFullComment(modelRow);
                        if (full != null && !full.isEmpty()) {
                            javax.swing.JOptionPane.showMessageDialog(MainPage.this, full, "Comment", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        installContextTracking();
    }

    /**
     * Creates a new MainPage and displays a personalized welcome message for
     * the logged-in user.
     *
     * @param userFullName the full name of the user to display
     */
    public MainPage(String userFullName) {
        this();
        setWelcomeName(userFullName);
    }

    /**
     * Updates the welcome label with the provided user's full name.
     *
     * @param userFullName the name to show in the welcome message
     */
    public void setWelcomeName(String userFullName) {
        jLabelWelcomeName.setText("<html> <center>Welcome<br>" + userFullName + "!</center></html>");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabelWelcomeName = new javax.swing.JLabel();
        jButtonViewFinalGrades = new javax.swing.JButton();
        jButtonViewStudents = new javax.swing.JButton();
        jButtonViewStudentGrades = new javax.swing.JButton();
        jButtonManageCourses = new javax.swing.JButton();
        jButtonManageAssignments = new javax.swing.JButton();
        jButtonStudentFeedback = new javax.swing.JButton();
        jButtonCreateReportCard = new javax.swing.JButton();
        jButtonAttendance = new javax.swing.JButton();
        iconViewStudents = new javax.swing.JLabel();
        iconViewStudentGrades = new javax.swing.JLabel();
        iconViewFinalGrades = new javax.swing.JLabel();
        iconAttendance = new javax.swing.JLabel();
        iconManageCourses = new javax.swing.JLabel();
        iconManageAssignments = new javax.swing.JLabel();
        iconStudentFeedback = new javax.swing.JLabel();
        iconCreateReportCard = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jComboBox = new javax.swing.JComboBox<>();
        jTextFieldSearch = new javax.swing.JTextField();
        jButtonSave = new javax.swing.JButton();
        jButtonEdit = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonAdd = new javax.swing.JButton();
        jButtonSearch = new javax.swing.JButton();
        jScrollPane = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        jPanel6 = new javax.swing.JPanel();
        jLabelMadeByJacquesSmit = new javax.swing.JLabel();
        jButtonHelp = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabelSGMS = new javax.swing.JLabel();
        jButtonLogout = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(javax.swing.UIManager.getDefaults().getColor("Button.default.foreground"));

        jPanel1.setForeground(new java.awt.Color(255, 255, 255));

        jPanel2.setBackground(new java.awt.Color(0, 153, 255));
        jPanel2.setForeground(new java.awt.Color(51, 102, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelWelcomeName.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabelWelcomeName.setForeground(new java.awt.Color(0, 0, 0));
        jLabelWelcomeName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelWelcomeName.setText("<html> <center>Welcome<br>Name!");
        jLabelWelcomeName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel2.add(jLabelWelcomeName, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 6, 217, 65));

        jButtonViewFinalGrades.setText("View Final Grades");
        jButtonViewFinalGrades.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewFinalGradesActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonViewFinalGrades, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 211, 150, 36));

        jButtonViewStudents.setText("View Students");
        jButtonViewStudents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewStudentsActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonViewStudents, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 83, 150, 36));

        jButtonViewStudentGrades.setText("View Student Grades");
        jButtonViewStudentGrades.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewStudentGradesActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonViewStudentGrades, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 147, 150, 36));

        jButtonManageCourses.setText("Manage Courses");
        jButtonManageCourses.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonManageCoursesActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonManageCourses, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 339, 150, 36));

        jButtonManageAssignments.setText("Manage Assignments");
        jButtonManageAssignments.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonManageAssignmentsActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonManageAssignments, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 403, 150, 36));

        jButtonStudentFeedback.setText("Student Feedback");
        jButtonStudentFeedback.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStudentFeedbackActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonStudentFeedback, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 467, 150, 36));

        jButtonCreateReportCard.setText("Create Report Card");
        jButtonCreateReportCard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateReportCardActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonCreateReportCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 531, 150, 36));

        jButtonAttendance.setText("Attendance");
        jButtonAttendance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAttendanceActionPerformed(evt);
            }
        });
        jPanel2.add(jButtonAttendance, new org.netbeans.lib.awtextra.AbsoluteConstraints(75, 275, 150, 36));

        iconViewStudents.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/view students.png"))); // NOI18N
        jPanel2.add(iconViewStudents, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

        iconViewStudentGrades.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/grades.png"))); // NOI18N
        jPanel2.add(iconViewStudentGrades, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 130, 50, -1));

        iconViewFinalGrades.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/final grades.png"))); // NOI18N
        jPanel2.add(iconViewFinalGrades, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, -1, -1));

        iconAttendance.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/attendance.png"))); // NOI18N
        jPanel2.add(iconAttendance, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 266, -1, -1));

        iconManageCourses.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/courses.png"))); // NOI18N
        jPanel2.add(iconManageCourses, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 327, -1, -1));

        iconManageAssignments.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/assignments.png"))); // NOI18N
        jPanel2.add(iconManageAssignments, new org.netbeans.lib.awtextra.AbsoluteConstraints(15, 392, -1, -1));

        iconStudentFeedback.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/feedback.png"))); // NOI18N
        jPanel2.add(iconStudentFeedback, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 460, -1, -1));

        iconCreateReportCard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/sgms/data/icons/print report.png"))); // NOI18N
        jPanel2.add(iconCreateReportCard, new org.netbeans.lib.awtextra.AbsoluteConstraints(11, 522, -1, -1));

        jPanel4.setForeground(java.awt.Color.white);
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxActionPerformed(evt);
            }
        });
        jPanel4.add(jComboBox, new org.netbeans.lib.awtextra.AbsoluteConstraints(18, 6, 272, 35));

        jTextFieldSearch.setText("Search");
        jTextFieldSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextFieldSearchFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextFieldSearchFocusLost(evt);
            }
        });
        jTextFieldSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldSearchActionPerformed(evt);
            }
        });
        jPanel4.add(jTextFieldSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 6, 253, 35));

        jButtonSave.setBackground(new java.awt.Color(6, 136, 6));
        jButtonSave.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jButtonSave.setText("Save");
        jButtonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveActionPerformed(evt);
            }
        });
        jPanel4.add(jButtonSave, new org.netbeans.lib.awtextra.AbsoluteConstraints(964, 10, 67, -1));

        jButtonEdit.setBackground(new java.awt.Color(204, 153, 0));
        jButtonEdit.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jButtonEdit.setText("Edit");
        jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditActionPerformed(evt);
            }
        });
        jPanel4.add(jButtonEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(891, 10, 67, -1));

        jButtonDelete.setBackground(new java.awt.Color(187, 52, 6));
        jButtonDelete.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jButtonDelete.setText("Delete");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        jPanel4.add(jButtonDelete, new org.netbeans.lib.awtextra.AbsoluteConstraints(815, 10, 70, -1));

        jButtonAdd.setBackground(new java.awt.Color(0, 153, 255));
        jButtonAdd.setFont(new java.awt.Font("Helvetica Neue", 0, 12)); // NOI18N
        jButtonAdd.setText("Add");
        jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddActionPerformed(evt);
            }
        });
        jPanel4.add(jButtonAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(742, 10, 67, -1));

        jButtonSearch.setBackground(new java.awt.Color(0, 153, 255));
        jButtonSearch.setFont(new java.awt.Font("Helvetica Neue", 1, 20)); // NOI18N
        jButtonSearch.setText("⌕");
        jButtonSearch.setActionCommand("");
        jButtonSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSearchActionPerformed(evt);
            }
        });
        jPanel4.add(jButtonSearch, new org.netbeans.lib.awtextra.AbsoluteConstraints(639, 10, 28, 28));

        jTable.setBackground(new java.awt.Color(255, 255, 255));
        jTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane.setViewportView(jTable);

        jPanel4.add(jScrollPane, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 50, 1020, 550));

        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(153, 153, 153)));
        jPanel6.setForeground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelMadeByJacquesSmit.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelMadeByJacquesSmit.setForeground(new java.awt.Color(0, 0, 0));
        jLabelMadeByJacquesSmit.setText("Made by Jacques Smit");
        jPanel6.add(jLabelMadeByJacquesSmit, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, -1));

        jButtonHelp.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButtonHelp.setForeground(new java.awt.Color(0, 0, 0));
        jButtonHelp.setText("Need help? Click here");
        jButtonHelp.setToolTipText("");
        jButtonHelp.setBorder(null);
        jButtonHelp.setBorderPainted(false);
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        jPanel6.add(jButtonHelp, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 10, 160, 20));

        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(153, 153, 153)));
        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabelSGMS.setFont(new java.awt.Font("Malgun Gothic", 1, 30)); // NOI18N
        jLabelSGMS.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelSGMS.setText("Student Grade Management System");
        jLabelSGMS.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabelSGMS.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel5.add(jLabelSGMS, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 0, -1, 50));

        jButtonLogout.setBackground(new java.awt.Color(255, 51, 51));
        jButtonLogout.setText("Logout");
        jButtonLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLogoutActionPerformed(evt);
            }
        });
        jPanel5.add(jButtonLogout, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 6, -1, -1));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 1043, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 603, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 598, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonViewStudentGradesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewStudentGradesActionPerformed
        // Switch the left-nav highlight
        setActiveButton(jButtonViewStudentGrades);

        // Show the course combo for picking which class's assignments to view
        loadCoursesForGrades();
        jComboBox.setEnabled(true);

        // Enable toolbar actions used on this screen
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);

        // Clear models that aren't used on this screen
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        finalGradesModel = null;
        studentSelectionModel = null;
        attendanceModel = null;
        feedbackModel = null;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        attendanceTodayColumn = -1;
        selectionMode = false;

        // Load the table
        loadStudentGradesForSelectedCourse();
    }//GEN-LAST:event_jButtonViewStudentGradesActionPerformed

    private void jButtonManageCoursesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManageCoursesActionPerformed
        setActiveButton(jButtonManageCourses);

        // Grade filter at the top
        jComboBox.setEnabled(true);
        jComboBox.removeAllItems();
        for (int g = 10; g <= 12; g++) {
            jComboBox.addItem("Grade " + g);
        }
        jComboBox.setSelectedIndex(0);

        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);

        // Clear anything not part of the Courses screen
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        selectionMode = false;
        attendanceTodayColumn = -1;

        // Load courses for the selected grade
        loadCoursesForSelectedGrade();
    }//GEN-LAST:event_jButtonManageCoursesActionPerformed

    private void jButtonViewStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewStudentsActionPerformed
        setActiveButton(jButtonViewStudents);

        // Course combo lets us filter the student list to a class, or show all
        loadCourses();
        jComboBox.setEnabled(true);

        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);

        // Clear models not used on this screen
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;

        studentDeleteColumn = null;
        studentDeleteMode = false;

        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;

        attendanceTodayColumn = -1;
        selectionMode = false;
        studentSelectionModel = null;

        // Load the table
        loadStudentsForSelectedCourse();
    }//GEN-LAST:event_jButtonViewStudentsActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        if (finalGradesModel != null) {
            return;
        }

        // ── Add a new Course ────────────────────────────────────────────────
        if (courseModel != null) {
            int grade = getSelectedGradeLevel();

            String code = javax.swing.JOptionPane.showInputDialog(this, "Enter course code:");
            if (code == null || !ValidationService.isNonEmpty(code) || !ValidationService.isValidCourseCode(code.trim())) {
                javax.swing.JOptionPane.showMessageDialog(this, "Invalid course code.");
                return;
            }
            code = code.trim();

            try {
                if (courseDAO.findByCode(code) != null) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Course code already exists.");
                    return;
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Error checking course code: " + ex.getMessage());
                return;
            }

            String name = javax.swing.JOptionPane.showInputDialog(this, "Enter course name:");
            if (name == null || !ValidationService.isNonEmpty(name)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Course name is required.");
                return;
            }
            name = name.trim();

            sgms.model.Course c = new sgms.model.Course(0, code, name, grade, 0);

            // Pause sorting so the new row is visible at the bottom
            if (jTable.getRowSorter() != null) {
                jTable.getRowSorter().setSortKeys(null);
            }

            courseModel.addCourse(c);
            int row = courseModel.getRowCount() - 1;
            jTable.setRowSelectionInterval(row, row);
            jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));
            return;
        }

        // ── Add a new Assignment ────────────────────────────────────────────
        if (assignmentModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }

            javax.swing.JTextField titleField = new javax.swing.JTextField();
            javax.swing.JTextField maxField = new javax.swing.JTextField();
            javax.swing.JTextField termField = new javax.swing.JTextField();
            javax.swing.JSpinner dateSpinner = new javax.swing.JSpinner(new javax.swing.SpinnerDateModel());
            dateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridLayout(0, 2));
            panel.add(new javax.swing.JLabel("Title:"));
            panel.add(titleField);
            panel.add(new javax.swing.JLabel("Max Marks:"));
            panel.add(maxField);
            panel.add(new javax.swing.JLabel("Term:"));
            panel.add(termField);
            panel.add(new javax.swing.JLabel("Due Date:"));
            panel.add(dateSpinner);

            int result = javax.swing.JOptionPane.showConfirmDialog(
                    this, panel, "New Assignment",
                    javax.swing.JOptionPane.OK_CANCEL_OPTION,
                    javax.swing.JOptionPane.PLAIN_MESSAGE);

            if (result == javax.swing.JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                if (!ValidationService.isNonEmpty(title)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Title is required.");
                    return;
                }

                String maxText = maxField.getText().trim();
                if (!ValidationService.isNonEmpty(maxText)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Max marks are required.");
                    return;
                }
                int max;
                try {
                    max = Integer.parseInt(maxText);
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Max marks must be a number.");
                    return;
                }
                if (!ValidationService.isIntInRange(max, 0, 1000)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Max marks must be between 0 and 1000.");
                    return;
                }

                String termText = termField.getText().trim();
                if (!ValidationService.isNonEmpty(termText)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Term is required.");
                    return;
                }
                int term;
                try {
                    term = Integer.parseInt(termText);
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Term must be a number between 1 and 4.");
                    return;
                }
                if (!ValidationService.isIntInRange(term, 1, 4)) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Term must be between 1 and 4.");
                    return;
                }

                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                java.time.LocalDate dueLocal = utilDate.toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                if (!ValidationService.isValidDueDate(dueLocal, java.time.LocalDate.now())) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Due date cannot be in the past.");
                    return;
                }
                java.sql.Date due = java.sql.Date.valueOf(dueLocal);

                sgms.model.Assignment a = new sgms.model.Assignment(0, courseId, title, max, term, due);

                if (jTable.getRowSorter() != null) {
                    jTable.getRowSorter().setSortKeys(null);
                }
                assignmentModel.addAssignment(a);
                int row = assignmentModel.getRowCount() - 1;
                jTable.setRowSelectionInterval(row, row);
                jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));
            }
            return;
        }

        // ── Add/Edit Feedback for the selected student ──────────────────────
        if (feedbackModel != null) {
            int row = jTable.getSelectedRow();
            if (row < 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Select a student first.");
                return;
            }
            int modelRow = jTable.convertRowIndexToModel(row);
            sgms.model.Student s = feedbackModel.getStudent(modelRow);
            String comment = javax.swing.JOptionPane.showInputDialog(
                    this, "Enter comment for " + s.getFirstName() + " " + s.getLastName() + ":");
            if (comment != null) {
                try {
                    feedbackDAO.upsert(s.getStudentId(), getSelectedCourseId(), comment);
                    loadFeedbackForSelectedCourse();
                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Unable to add feedback: " + ex.getMessage(),
                            "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        // ── Add students to a course (enrolment) ────────────────────────────
        if (studentGradesModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId > 0 && !selectionMode) {       // fixed: > 0 and logical &&
                startEnrollmentEdit();
            }
            return;
        }

        // ── Add a new Student row ───────────────────────────────────────────
        if (studentTableModel != null) {
            try {
                sgms.model.Student s = studentDAO.add(new sgms.model.Student("", "", 0));

                if (jTable.getRowSorter() != null) {
                    jTable.getRowSorter().setSortKeys(null);
                }

                studentTableModel.addStudent(s);
                int row = studentTableModel.getRowCount() - 1;
                jTable.setRowSelectionInterval(row, row);
                jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));

                // Start editing First Name (col 2). ID (col 1) is read-only.
                jTable.editCellAt(row, 2);
                java.awt.Component editor = jTable.getEditorComponent();
                if (editor != null) {
                    editor.requestFocusInWindow();
                }
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, "Unable to add student: " + ex.getMessage(),
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if (finalGradesModel != null) {
            return;
        }

        // Feedback keeps old immediate delete
        if (feedbackModel != null) {
            int r = jTable.getSelectedRow();
            if (r >= 0) {
                int m = jTable.convertRowIndexToModel(r);
                Student s = feedbackModel.getStudent(m);
                int ok = JOptionPane.showConfirmDialog(this,
                        "Delete comment for " + s.getFirstName() + " " + s.getLastName() + "?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (ok == JOptionPane.YES_OPTION) {
                    try {
                        feedbackDAO.delete(s.getStudentId(), getSelectedCourseId());
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Unable to delete feedback: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    reloadCurrentGrid();
                }
            }
            return;
        }

        if (studentTableModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId > 0) { // delete = enrolment edit in a course context
                if (!selectionMode) {
                    startEnrollmentEdit();
                }
                return;
            }
            studentDeleteMode = !studentDeleteMode;
            reloadCurrentGrid();
            return;
        }

        if (assignmentModel != null) {
            assignmentDeleteMode = !assignmentDeleteMode;
            reloadCurrentGrid();
            return;
        }

        if (courseModel != null) {
            courseDeleteMode = !courseDeleteMode;
            reloadCurrentGrid();
            return;
        }

        // Grades grid: just refresh
        reloadCurrentGrid();
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxActionPerformed
        if (studentTableModel != null) {
            loadStudentsForSelectedCourse();
        } else if (studentGradesModel != null) {
            loadStudentGradesForSelectedCourse();
        } else if (finalGradesModel != null) {
            loadFinalGradesForSelectedGrade();
        } else if (attendanceModel != null) {
            loadAttendanceForSelectedCourse();
        } else if (feedbackModel != null) {
            loadFeedbackForSelectedCourse();
        } else if (assignmentModel != null) {
            loadAssignmentsForSelectedCourse();
        } else if (courseModel != null) {
            loadCoursesForSelectedGrade();
        }
    }//GEN-LAST:event_jComboBoxActionPerformed

    private void jTextFieldSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldSearchActionPerformed
        sgms.util.SearchUtil.applySmartSearch(jTable, jTextFieldSearch.getText());
    }//GEN-LAST:event_jTextFieldSearchActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        String message;
        if (lastActionButton == null) {
            message = "Press a button first to get contextual help. "
                    + "The table supports sorting by clicking column headers and filtering with the Search box.";
        } else if (lastActionButton == jButtonViewStudents) {
            message = "View Students shows learners in the selected course.\n"
                    + "Use the Search box to filter and click column headers to sort. Edit names or grades directly.";
        } else if (lastActionButton == jButtonViewStudentGrades) {
            message = "View Student Grades lists raw marks per assignment.\n"
                    + "Double-click a mark to edit it out of the assignment’s max. The app converts raw marks to percentages for term/final and report cards.";
        } else if (lastActionButton == jButtonViewFinalGrades) {
            message = "View Final Grades shows each learner’s term averages across all courses in the selected grade, "
                    + "and the weighted final (T1 12.5%, T2 25%, T3 12.5%, T4 50%).\n"
                    + "You can sort and filter these columns like the others.";
        } else if (lastActionButton == jButtonAttendance) {
            message = "Attendance lets you mark daily presence.\n"
                    + "The yellow column is today—click cells to toggle present/absent, then press Save.";
        } else if (lastActionButton == jButtonStudentFeedback) {
            message = "Student Feedback lets you add or edit comments.\n"
                    + "Click a comment cell to view the full text. Use Search and sorting to find entries.";
        } else if (lastActionButton == jButtonManageCourses) {
            message = "Manage Courses lets you add, rename, or mark courses for deletion.\n"
                    + "Rows ticked in the Delete column turn red until you Save.";
        } else if (lastActionButton == jButtonManageAssignments) {
            message = "Manage Assignments is for adding/editing assignments (title, max marks, term, due date).\n"
                    + "Use Delete to mark rows, then Save to commit.";
        } else if (lastActionButton == jButtonCreateReportCard) {
            message = "Create Report Card builds a Word (.docx) report for the selected student using the current data.\n"
                    + "Select a student row first, then click this button.";
        } else if (lastActionButton == jButtonAdd) {
            message = "Add inserts a new row for the current view. In Student Grades, it opens the enrolment editor to add/remove learners from the course.";
        } else if (lastActionButton == jButtonDelete) {
            message = "Delete marks the selected row for deletion (red) or, in Student Grades, opens the enrolment editor.\n"
                    + "For Feedback it deletes the selected comment after confirmation.";
        } else if (lastActionButton == jButtonSave) {
            message = "Save writes your changes to the database.";
        } else if (lastActionButton == jButtonEdit) {
            message = "Edit enables editing where it is disabled on the current view.";
        } else if (lastActionButton == jButtonSearch) {
            message = "Search filters the table according to the text entered in the Search box.\n"
                    + "Click column headers to sort the results.";
        } else {
            message = "Press a button first to get contextual help. "
                    + "The table supports sorting by clicking column headers and filtering with the Search box.";
        }
        javax.swing.JOptionPane.showMessageDialog(this, message, "Help", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jButtonLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLogoutActionPerformed
        LoginPage loginPage = new LoginPage();
        loginPage.setVisible(true);
        dispose();
        // Close the shared DB connection to free resources when logging out
        DB.shutdown();
    }//GEN-LAST:event_jButtonLogoutActionPerformed

    private void jTextFieldSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldSearchFocusGained
        if ("Search".equals(jTextFieldSearch.getText())) {
            jTextFieldSearch.setText("");
            jTextFieldSearch.setForeground(Color.BLACK);
        }
    }//GEN-LAST:event_jTextFieldSearchFocusGained

    private void jTextFieldSearchFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextFieldSearchFocusLost
        if (jTextFieldSearch.getText().isEmpty()) {
            jTextFieldSearch.setText("Search");
            jTextFieldSearch.setForeground(Color.GRAY);
        }
    }//GEN-LAST:event_jTextFieldSearchFocusLost

    private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
        if (jTable.isEditing()) {
            jTable.getCellEditor().stopCellEditing();
        }

        // ───────────────── ASSIGNMENTS ─────────────────
        if (assignmentModel != null) {
            try {
                java.util.Set<Integer> deleted = assignmentModel.getDeletedIds();
                for (Assignment a : assignmentModel.getAssignments()) {
                    int id = a.getAssignmentId();
                    if (deleted.contains(Integer.valueOf(id))) {
                        if (id > 0) {
                            assignmentDAO.delete(id);
                        }
                        continue;
                    }
                    if (id == 0) {
                        a.setCourseId(getSelectedCourseId());
                        assignmentDAO.add(a);
                    } else {
                        assignmentDAO.update(a);
                    }
                }
                assignmentDeleteMode = false;
                reloadCurrentGrid();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save assignments: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // ───────────────── COURSES ─────────────────
        if (courseModel != null) {
            try {
                java.util.Set<Integer> deleted = courseModel.getDeletedIds();
                for (Course c : courseModel.getCourses()) {
                    int id = c.getCourseId();
                    if (deleted.contains(Integer.valueOf(id))) {
                        if (id > 0) {
                            courseDAO.delete(id);
                        }
                        continue;
                    }
                    if (id == 0) {
                        courseDAO.add(c);
                    } else {
                        courseDAO.update(c);
                    }
                }
                courseDeleteMode = false;
                reloadCurrentGrid();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save courses: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // ───────────────── STUDENTS (View Students table) ─────────────────
        if (studentTableModel == null) {
            return;
        }

        java.util.Set<Integer> deleted = studentTableModel.getDeletedIds();

        // validate non-deleted rows
        for (Student s : studentTableModel.getStudents()) {
            int id = s.getStudentId();
            if (deleted.contains(Integer.valueOf(id))) {
                continue; // skip validation for rows marked to delete
            }
            String fn = s.getFirstName();
            String ln = s.getLastName();
            if (fn == null || fn.trim().isEmpty() || ln == null || ln.trim().isEmpty() || s.getGradeLevel() <= 0) {
                JOptionPane.showMessageDialog(this, "All student fields must be filled.", "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            for (Student s : studentTableModel.getStudents()) {
                int id = s.getStudentId();
                if (deleted.contains(Integer.valueOf(id))) {
                    if (id > 0) {
                        studentDAO.delete(id);
                    }
                    continue;
                }
                if (id == 0) {
                    studentDAO.add(s);
                } else {
                    studentDAO.update(s);
                }
            }

            studentTableModel.clearDeleted();
            studentDeleteMode = false;
            reloadCurrentGrid();
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save students: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        // Final grades is read-only
        if (finalGradesModel != null) {
            return;
        }

        // If we're in enrolment selection mode (checkbox table)
        if (selectionMode && studentSelectionModel != null) {
            if (jTable.getRowCount() > 0) {
                startEditAt(0, 0); // checkbox column
            }
            return;
        }

        // Manage Courses: edit Name column
        if (courseModel != null) {
            if (jTable.getRowCount() > 0) {
                startEditAtFirstEditable(0, 3 - 1); // visual col 2 (index 2): Name
            }
            return;
        }

        // Manage Assignments: edit Title column
        if (assignmentModel != null) {
            if (jTable.getRowCount() > 0) {
                startEditAtFirstEditable(0, 3 - 1); // visual col 2 (index 2): Title
            }
            return;
        }

        // Feedback: edit Comment column
        if (feedbackModel != null) {
            if (jTable.getRowCount() > 0) {
                startEditAtFirstEditable(0, 3 - 1); // visual col 2 (index 2): Comment
            }
            return;
        }

        // Attendance: start on today’s column if known, else first day
        if (attendanceModel != null) {
            if (jTable.getRowCount() > 0) {
                int col = (attendanceTodayColumn >= 0 && attendanceTodayColumn < jTable.getColumnCount())
                        ? attendanceTodayColumn
                        : 1; // first day column (col 0 is "Student")
                startEditAt(0, col);
            }
            return;
        }

        // Student Grades: first assignment column
        if (studentGradesModel != null) {
            if (jTable.getRowCount() > 0 && jTable.getColumnCount() > 1) {
                startEditAt(0, 1);
            }
            return;
        }

        // View Students table
        if (studentTableModel == null) {
            return;
        }

        // If a course is selected, Edit means “edit enrolments”
        int courseId = getSelectedCourseId();
        if (courseId > 0) {
            if (!selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }

        // Otherwise, edit current cell if it's editable; else go to first editable cell
        int row = jTable.getSelectedRow();
        int col = jTable.getSelectedColumn();
        if (row >= 0 && col >= 0 && jTable.isCellEditable(row, col)) {
            jTable.editCellAt(row, col);
            java.awt.Component ed = jTable.getEditorComponent();
            if (ed instanceof javax.swing.text.JTextComponent) {
                javax.swing.text.JTextComponent tc = (javax.swing.text.JTextComponent) ed;
                tc.requestFocusInWindow();
                tc.selectAll();
            } else if (ed != null) {
                ed.requestFocusInWindow();
            }
        } else if (jTable.getRowCount() > 0) {
            int preferred = studentDeleteMode ? 0 : 2; // Delete checkbox or First Name
            startEditAtFirstEditable(0, preferred);
        }
    }

    /**
     * Moves to (viewRow, viewCol), starts editing, and selects text if it's a
     * text cell.
     */
    private void startEditAt(int viewRow, int viewCol) {
        if (viewRow < 0 || viewCol < 0) {
            return;
        }
        if (viewRow >= jTable.getRowCount() || viewCol >= jTable.getColumnCount()) {
            return;
        }
        if (!jTable.isCellEditable(viewRow, viewCol)) {
            // find next editable column in the same row
            for (int c = viewCol; c < jTable.getColumnCount(); c++) {
                if (jTable.isCellEditable(viewRow, c)) {
                    viewCol = c;
                    break;
                }
            }
        }
        jTable.changeSelection(viewRow, viewCol, false, false);
        jTable.editCellAt(viewRow, viewCol);
        java.awt.Component editor = jTable.getEditorComponent();
        if (editor instanceof javax.swing.text.JTextComponent) {
            javax.swing.text.JTextComponent tc = (javax.swing.text.JTextComponent) editor;
            tc.requestFocusInWindow();
            tc.selectAll();
        } else if (editor != null) {
            editor.requestFocusInWindow();
        }
    }

    /**
     * Tries preferredCol; if not editable, scans for the first editable column
     * in that row.
     */
    private void startEditAtFirstEditable(int row, int preferredCol) {
        if (jTable.getRowCount() == 0) {
            return;
        }
        int col = preferredCol;
        if (col < 0 || col >= jTable.getColumnCount() || !jTable.isCellEditable(row, col)) {
            // scan left-to-right
            for (int c = 0; c < jTable.getColumnCount(); c++) {
                if (jTable.isCellEditable(row, c)) {
                    col = c;
                    break;
                }
            }
        }
        startEditAt(row, col);
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        sgms.util.SearchUtil.applySmartSearch(jTable, jTextFieldSearch.getText());
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private void jButtonViewFinalGradesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewFinalGradesActionPerformed
        setActiveButton(jButtonViewFinalGrades);

        // Combo shows grade levels for this view
        loadGradeLevelsForFinalGrades();
        jComboBox.setEnabled(true);

        // Final grades is read-only
        jButtonAdd.setEnabled(false);
        jButtonDelete.setEnabled(false);
        jButtonSave.setEnabled(false);
        jButtonEdit.setEnabled(false);

        // Clear table state/models
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        studentGradesModel = null;
        studentSelectionModel = null;
        attendanceModel = null;
        feedbackModel = null;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        finalGradesModel = null;
        attendanceTodayColumn = -1;
        selectionMode = false;

        // Load the table
        loadFinalGradesForSelectedGrade();
    }//GEN-LAST:event_jButtonViewFinalGradesActionPerformed

    private void jButtonAttendanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAttendanceActionPerformed
        setActiveButton(jButtonAttendance);

        loadCoursesForAttendance();
        jComboBox.setEnabled(true);

        // Only Save is needed here
        jButtonAdd.setEnabled(false);
        jButtonDelete.setEnabled(false);
        jButtonEdit.setEnabled(false);
        jButtonSave.setEnabled(true);

        // Clear other models/state
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        feedbackModel = null;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        selectionMode = false;
        attendanceTodayColumn = -1;

        // Load the table
        loadAttendanceForSelectedCourse();
    }//GEN-LAST:event_jButtonAttendanceActionPerformed

    private void jButtonStudentFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStudentFeedbackActionPerformed
        setActiveButton(jButtonStudentFeedback);

        loadCoursesForFeedback();
        jComboBox.setEnabled(true);

        // Feedback allows full CRUD
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonEdit.setEnabled(true);
        jButtonSave.setEnabled(true);

        // Clear other models/state
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;  // set by loader
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        selectionMode = false;

        // Load the table
        loadFeedbackForSelectedCourse();
    }//GEN-LAST:event_jButtonStudentFeedbackActionPerformed

    private void jButtonManageAssignmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManageAssignmentsActionPerformed
        setActiveButton(jButtonManageAssignments);

        loadCoursesForAssignments();
        jComboBox.setEnabled(true);

        // Full CRUD on assignments
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);

        // Clear other models/state
        studentTableModel = null;
        studentDeleteColumn = null;
        studentDeleteMode = false;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        assignmentModel = null;         // set by loader
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        selectionMode = false;
        attendanceTodayColumn = -1;

        // Loads the table
        loadAssignmentsForSelectedCourse();
    }//GEN-LAST:event_jButtonManageAssignmentsActionPerformed

    private void jButtonCreateReportCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateReportCardActionPerformed
        final Student selected = getSelectedStudent();
        if (selected == null) {
            return; // no row selected
        }

        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
        jButtonCreateReportCard.setEnabled(false);

        new javax.swing.SwingWorker<java.nio.file.Path, Void>() {
            @Override
            protected java.nio.file.Path doInBackground() throws Exception {
                // Build merge data and create the DOCX in the background
                java.util.Map<String, String> data = sgms.util.ReportCardGenerator.buildData(selected);
                return sgms.util.ReportCardGenerator.generateDocx(data);
            }

            @Override
            protected void done() {
                try {
                    // If doInBackground threw, this rethrows the exception
                    java.nio.file.Path docx = get();
                    sgms.util.ReportCardGenerator.openFile(docx);
                } catch (Exception ex) {
                    javax.swing.JOptionPane.showMessageDialog(
                            MainPage.this,
                            "Unable to create report card: " + ex.getMessage(),
                            "Error",
                            javax.swing.JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    jButtonCreateReportCard.setEnabled(true);
                    setCursor(java.awt.Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }//GEN-LAST:event_jButtonCreateReportCardActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainPage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel iconAttendance;
    private javax.swing.JLabel iconCreateReportCard;
    private javax.swing.JLabel iconManageAssignments;
    private javax.swing.JLabel iconManageCourses;
    private javax.swing.JLabel iconStudentFeedback;
    private javax.swing.JLabel iconViewFinalGrades;
    private javax.swing.JLabel iconViewStudentGrades;
    private javax.swing.JLabel iconViewStudents;
    private javax.swing.JButton jButtonAdd;
    private javax.swing.JButton jButtonAttendance;
    private javax.swing.JButton jButtonCreateReportCard;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonEdit;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonLogout;
    private javax.swing.JButton jButtonManageAssignments;
    private javax.swing.JButton jButtonManageCourses;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSearch;
    private javax.swing.JButton jButtonStudentFeedback;
    private javax.swing.JButton jButtonViewFinalGrades;
    private javax.swing.JButton jButtonViewStudentGrades;
    private javax.swing.JButton jButtonViewStudents;
    private javax.swing.JComboBox<Object> jComboBox;
    private javax.swing.JLabel jLabelMadeByJacquesSmit;
    private javax.swing.JLabel jLabelSGMS;
    private javax.swing.JLabel jLabelWelcomeName;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTable jTable;
    private javax.swing.JTextField jTextFieldSearch;
    // End of variables declaration//GEN-END:variables

    /**
     * Loads the course list into the combo box. The first entry is a sentinel
     * "All Students" (courseId = 0).
     */
    private void loadCourses() {
        try {
            List<Course> courses = studentDAO.findCourses();

            // Reset the combo box so we don’t duplicate items
            jComboBox.removeAllItems();

            // Add sentinel option (courseId 0 means “no specific course filter”)
            jComboBox.addItem(new Course(0, "All Students"));

            // Add each real course object (toString() shows the name)
            for (Course c : courses) {
                jComboBox.addItem(c);
            }

            // Default to the first option
            jComboBox.setSelectedIndex(0);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load courses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Returns the selected courseId from the combo box. If “All Students” or
     * nothing is selected, returns 0.
     */
    private int getSelectedCourseId() {
        Object sel = jComboBox.getSelectedItem();
        if (sel instanceof Course) {
            Course c = (Course) sel;
            return c.getCourseId(); // 0 for “All Students”
        }
        return 0;
    }

    /**
     * Reads a grade level from the combo box. The combo can hold either an
     * Integer (e.g. 10) or a String like "Grade 10". Returns 0 if we can’t
     * parse a valid number.
     */
    private int getSelectedGradeLevel() {
        Object sel = jComboBox.getSelectedItem();

        // Case 1: value already stored as Integer
        if (sel instanceof Integer) {
            return ((Integer) sel).intValue();
        }

        // Case 2: value is a String like "Grade 10" → strip non-digits
        if (sel instanceof String) {
            try {
                String digits = ((String) sel).replaceAll("\\D", ""); // keep only numbers
                return digits.isEmpty() ? 0 : Integer.parseInt(digits);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }

        // Unknown type or nothing selected
        return 0;
    }

    /**
     * Returns the Student currently selected in the table (view row),
     * converting to the model row first. Shows a message if nothing is
     * selected.
     */
    private Student getSelectedStudent() {
        // Only valid when the “View Students” table model is active
        if (studentTableModel == null) {
            JOptionPane.showMessageDialog(this,
                    "Please view students and select a record first.");
            return null;
        }

        // Row selected in the *view* (may be sorted/filtered)
        int viewRow = jTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student from the table.");
            return null;
        }

        // Map view index → model index, then fetch the Student object
        int modelRow = jTable.convertRowIndexToModel(viewRow);
        return studentTableModel.getStudent(modelRow);
    }

    /**
     * Loads courses for the selected grade and displays them in the Manage
     * Courses table.
     */
    private void loadCoursesForSelectedGrade() {
        try {
            // 1) Read grade from combo and fetch matching courses
            int grade = getSelectedGradeLevel();
            List<Course> courses = courseDAO.findByGrade(grade);

            // 2) Build and attach the table model
            courseModel = new CourseTableModel(courses);
            jTable.setModel(courseModel);

            // 3) Enable sorting for this model
            jTable.setRowSorter(new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(courseModel));

            // 4) Left-align table header text (only if the default renderer is a DefaultTableCellRenderer)
            java.awt.Component hdrRend = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdrRend instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

            // 5) Editors/renderers: left-aligned checkbox for Boolean; left-aligned text for others
            javax.swing.JCheckBox editorCheckBox = new javax.swing.JCheckBox();
            editorCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.setDefaultEditor(Boolean.class, new javax.swing.DefaultCellEditor(editorCheckBox));

            for (int i = 0; i < jTable.getColumnCount(); i++) {
                javax.swing.table.TableColumn column = jTable.getColumnModel().getColumn(i);
                if (jTable.getColumnClass(i) == Boolean.class) {
                    column.setCellRenderer(new javax.swing.table.TableCellRenderer() {
                        final javax.swing.JCheckBox cb = new javax.swing.JCheckBox();

                        {
                            cb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                            cb.setOpaque(true);
                        }

                        @Override
                        public java.awt.Component getTableCellRendererComponent(
                                javax.swing.JTable table, Object value, boolean isSelected,
                                boolean hasFocus, int row, int col) {
                            cb.setSelected(Boolean.TRUE.equals(value));
                            if (isSelected) {
                                cb.setBackground(table.getSelectionBackground());
                                cb.setForeground(table.getSelectionForeground());
                            } else {
                                cb.setBackground(table.getBackground());
                                cb.setForeground(table.getForeground());
                            }
                            return cb;
                        }
                    });
                } else {
                    javax.swing.table.DefaultTableCellRenderer left = new javax.swing.table.DefaultTableCellRenderer();
                    left.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    column.setCellRenderer(left);
                }
            }

            // Show/hide the Delete column based on courseDeleteMode
            javax.swing.table.TableColumn[] ref = {courseDeleteColumn};
            applyDeleteVisibility(ref, courseDeleteMode);
            courseDeleteColumn = ref[0];

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load courses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads students for the selected course (or all students) into the View
     * Students table.
     */
    private void loadStudentsForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();

            // 1) Fetch students for the current filter
            java.util.List<sgms.model.Student> students;
            if (courseId > 0) {
                students = studentDAO.findByCourse(courseId);
            } else {
                students = studentDAO.findAll();
            }

            // 2) Clear other modes/models so the table behaves like a plain “students” grid
            studentGradesModel = null;
            finalGradesModel = null;
            selectionMode = false;
            studentSelectionModel = null;

            // 3) Reuse the existing StudentTableModel if present, otherwise create a new one
            if (jTable.getModel() instanceof sgms.ui.StudentTableModel) {
                studentTableModel = (sgms.ui.StudentTableModel) jTable.getModel();
                studentTableModel.clearDeleted();
                studentTableModel.setRows(students);
            } else {
                studentTableModel = new sgms.ui.StudentTableModel(students);
                jTable.setModel(studentTableModel);
            }

            // 4) Fresh TableRowSorter for this model (so comparators/keys apply reliably)
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(jTable.getModel());
            jTable.setRowSorter(sorter);

            // Use a case/diacritics-insensitive Collator for First/Last name sorting
            java.text.Collator coll = java.text.Collator.getInstance(java.util.Locale.ROOT);
            coll.setStrength(java.text.Collator.PRIMARY);
            sorter.setComparator(2, coll); // First Name
            sorter.setComparator(3, coll); // Last Name

            // Default sort order: Grade ↑, Last Name ↑, First Name ↑
            java.util.List<javax.swing.RowSorter.SortKey> keys
                    = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
            keys.add(new javax.swing.RowSorter.SortKey(4, javax.swing.SortOrder.ASCENDING));
            keys.add(new javax.swing.RowSorter.SortKey(3, javax.swing.SortOrder.ASCENDING));
            keys.add(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();

            // 5) Header and cell alignment: left for everything, with a left-aligned checkbox editor
            java.awt.Component hdrRend = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdrRend instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

            javax.swing.JCheckBox editorCheckBox = new javax.swing.JCheckBox();
            editorCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.setDefaultEditor(Boolean.class, new javax.swing.DefaultCellEditor(editorCheckBox));

            for (int i = 0; i < jTable.getColumnCount(); i++) {
                javax.swing.table.TableColumn column = jTable.getColumnModel().getColumn(i);
                if (jTable.getColumnClass(i) == Boolean.class) {
                    column.setCellRenderer(new javax.swing.table.TableCellRenderer() {
                        final javax.swing.JCheckBox cb = new javax.swing.JCheckBox();

                        {
                            cb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                            cb.setOpaque(true);
                        }

                        @Override
                        public java.awt.Component getTableCellRendererComponent(
                                javax.swing.JTable table, Object value, boolean isSelected,
                                boolean hasFocus, int row, int col) {
                            cb.setSelected(Boolean.TRUE.equals(value));
                            if (isSelected) {
                                cb.setBackground(table.getSelectionBackground());
                                cb.setForeground(table.getSelectionForeground());
                            } else {
                                cb.setBackground(table.getBackground());
                                cb.setForeground(table.getForeground());
                            }
                            return cb;
                        }
                    });
                } else {
                    javax.swing.table.DefaultTableCellRenderer left = new javax.swing.table.DefaultTableCellRenderer();
                    left.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    column.setCellRenderer(left);
                }
            }

            // Show/hide the Delete column based on studentDeleteMode
            javax.swing.table.TableColumn[] ref = {studentDeleteColumn};
            applyDeleteVisibility(ref, studentDeleteMode);
            studentDeleteColumn = ref[0];

            // 7) Basic column width distribution so the table looks balanced
            int cols = jTable.getColumnModel().getColumnCount();
            int w = 0;
            java.awt.Container parent = jTable.getParent();
            if (parent != null) {
                w = parent.getWidth();
            }
            if (w <= 0) {
                w = jTable.getWidth();
            }
            int per = (cols > 0 && w > 0) ? Math.max(60, w / cols) : 100;
            for (int i = 0; i < cols; i++) {
                jTable.getColumnModel().getColumn(i).setPreferredWidth(per);
            }
            jTable.doLayout();

            // 8) Refresh visuals
            jTable.clearSelection();
            jTable.revalidate();
            jTable.repaint();

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Unable to load students: " + ex.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Opens the enrolment editor (checkbox list of all students for the
     * selected course).
     */
    private void startEnrollmentEdit() {
        try {
            int courseId = getSelectedCourseId();

            // Build the "initially selected" set = students already enrolled on this course
            List<Student> all = studentDAO.findAll();
            List<Student> enrolled = studentDAO.findByCourse(courseId);
            Set<Integer> initial = new HashSet<Integer>();
            for (int i = 0; i < enrolled.size(); i++) {
                initial.add(enrolled.get(i).getStudentId());
            }

            // Table model with first column = checkbox
            studentSelectionModel = new StudentSelectionTableModel(all, initial);
            jTable.setModel(studentSelectionModel);

            // Fresh sorter for this model
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(jTable.getModel());
            jTable.setRowSorter(sorter);

            // Sort by: Grade ↑, Last Name ↑, First Name ↑
            java.util.List<javax.swing.RowSorter.SortKey> keys
                    = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
            keys.add(new javax.swing.RowSorter.SortKey(4, javax.swing.SortOrder.ASCENDING));
            keys.add(new javax.swing.RowSorter.SortKey(3, javax.swing.SortOrder.ASCENDING));
            keys.add(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();

            // Case/diacritics-insensitive sort for names
            java.text.Collator coll = java.text.Collator.getInstance(java.util.Locale.ROOT);
            coll.setStrength(java.text.Collator.PRIMARY);
            sorter.setComparator(2, coll); // First Name
            sorter.setComparator(3, coll); // Last  Name

            // Header text left-aligned (only if header renderer supports it)
            java.awt.Component hdr = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdr instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

            // Left-aligned checkbox editor/renderer for the Select column
            javax.swing.JCheckBox editorCheck = new javax.swing.JCheckBox();
            editorCheck.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.setDefaultEditor(Boolean.class, new javax.swing.DefaultCellEditor(editorCheck));

            jTable.getColumnModel().getColumn(0).setCellRenderer(new javax.swing.table.TableCellRenderer() {
                final javax.swing.JCheckBox cb = new javax.swing.JCheckBox();

                {
                    cb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    cb.setOpaque(true);
                }

                @Override
                public java.awt.Component getTableCellRendererComponent(
                        javax.swing.JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    cb.setSelected(Boolean.TRUE.equals(value));
                    if (isSelected) {
                        cb.setBackground(table.getSelectionBackground());
                        cb.setForeground(table.getSelectionForeground());
                    } else {
                        cb.setBackground(table.getBackground());
                        cb.setForeground(table.getForeground());
                    }
                    return cb;
                }
            });

            // Left-align ID and Grade columns for a consistent look
            javax.swing.table.DefaultTableCellRenderer left = new javax.swing.table.DefaultTableCellRenderer();
            left.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.getColumnModel().getColumn(1).setCellRenderer(left); // ID
            jTable.getColumnModel().getColumn(4).setCellRenderer(left); // Grade

            selectionMode = true;                 // lets the rest of the UI know we're editing enrolments
            setActiveButton(jButtonViewStudents); // keep the side button highlighting in sync

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load students: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the “Student Grades” grid for the selected course (raw marks per
     * assignment).
     */
    private void loadStudentGradesForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return; // no course picked
            }

            // Fetch rows and columns
            List<Student> students = studentDAO.findByCourse(courseId);
            List<Assignment> assignments = assignmentDAO.findByCourse(courseId);
            Map<Integer, Map<Integer, Integer>> grades = gradeDAO.findByCourse(courseId);

            // Build model and detach other modes
            studentGradesModel = new StudentGradesTableModel(students, assignments, grades);
            finalGradesModel = null;
            studentTableModel = null;
            studentDeleteColumn = null;
            studentDeleteMode = false;
            selectionMode = false;
            studentSelectionModel = null;

            jTable.setModel(studentGradesModel);

            // Fresh sorter; default by student name ascending
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(jTable.getModel());
            jTable.setRowSorter(sorter);

            java.util.List<javax.swing.RowSorter.SortKey> keys
                    = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
            keys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();

            // Left-align header text safely
            java.awt.Component hdr = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdr instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load grades: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Builds the “Final Grades” view for a grade level: - pulls all students in
     * that grade - merges all assignments from all courses in that grade -
     * merges raw marks by student - uses StudentGradesTableModel helpers to get
     * T1..T4% and a weighted final
     */
    private void loadFinalGradesForSelectedGrade() {
        try {
            int gradeLevel = getSelectedGradeLevel();

            // 1) Students in this grade
            java.util.List<sgms.model.Student> all = studentDAO.findAll();
            java.util.List<sgms.model.Student> students = new java.util.ArrayList<sgms.model.Student>();
            for (int i = 0; i < all.size(); i++) {
                sgms.model.Student s = all.get(i);
                if (s.getGradeLevel() == gradeLevel) {
                    students.add(s);
                }
            }

            // 2) Merge assignments + grades from ALL courses in the grade
            java.util.List<sgms.model.Assignment> assignments = new java.util.ArrayList<sgms.model.Assignment>();
            java.util.Map<Integer, java.util.Map<Integer, Integer>> mergedGrades
                    = new java.util.HashMap<Integer, java.util.Map<Integer, Integer>>();

            java.util.List<sgms.model.Course> courses = courseDAO.findByGrade(gradeLevel);
            for (int ci = 0; ci < courses.size(); ci++) {
                sgms.model.Course c = courses.get(ci);

                // collect assignments
                java.util.List<sgms.model.Assignment> assigns = assignmentDAO.findByCourse(c.getCourseId());
                for (int j = 0; j < assigns.size(); j++) {
                    assignments.add(assigns.get(j));
                }

                // merge grades into mergedGrades (studentId -> (assignmentId -> raw mark))
                java.util.Map<Integer, java.util.Map<Integer, Integer>> gByStu = gradeDAO.findByCourse(c.getCourseId());
                java.util.Iterator<java.util.Map.Entry<Integer, java.util.Map<Integer, Integer>>> it = gByStu.entrySet().iterator();
                while (it.hasNext()) {
                    java.util.Map.Entry<Integer, java.util.Map<Integer, Integer>> e = it.next();
                    Integer studentId = e.getKey();
                    java.util.Map<Integer, Integer> src = e.getValue();

                    java.util.Map<Integer, Integer> dest = mergedGrades.get(studentId);
                    if (dest == null) {
                        dest = new java.util.HashMap<Integer, Integer>();
                        mergedGrades.put(studentId, dest);
                    }
                    java.util.Iterator<java.util.Map.Entry<Integer, Integer>> it2 = src.entrySet().iterator();
                    while (it2.hasNext()) {
                        java.util.Map.Entry<Integer, Integer> g = it2.next();
                        dest.put(g.getKey(), g.getValue());
                    }
                }
            }

            // 3) Backing grid (provides the term % helpers)
            sgms.ui.StudentGradesTableModel backing
                    = new sgms.ui.StudentGradesTableModel(students, assignments, mergedGrades);

            // 4) Final summary model (T1..T4 + weighted final)
            finalGradesModel = new sgms.ui.FinalGradesTableModel(students, backing);

            // Apply to table and clear other modes
            studentGradesModel = null;
            studentTableModel = null;
            studentDeleteColumn = null;
            studentDeleteMode = false;
            selectionMode = false;
            studentSelectionModel = null;

            jTable.setModel(finalGradesModel);

            // Sort: Final % ↓, Last Name ↑, First Name ↑
            jTable.setAutoCreateRowSorter(true);
            javax.swing.RowSorter<?> rs = jTable.getRowSorter();
            if (rs instanceof javax.swing.table.TableRowSorter) {
                @SuppressWarnings("unchecked")
                javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                        = (javax.swing.table.TableRowSorter<javax.swing.table.TableModel>) rs;

                java.text.Collator coll = java.text.Collator.getInstance(java.util.Locale.ROOT);
                coll.setStrength(java.text.Collator.PRIMARY);
                sorter.setComparator(0, coll); // First Name
                sorter.setComparator(1, coll); // Last  Name

                java.util.List<javax.swing.RowSorter.SortKey> keys
                        = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
                keys.add(new javax.swing.RowSorter.SortKey(6, javax.swing.SortOrder.DESCENDING)); // Final %
                keys.add(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING));  // Last
                keys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));  // First
                sorter.setSortKeys(keys);
                sorter.sort();
            }

            // Header text left-aligned (safe check)
            java.awt.Component hdr = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdr instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

            // Even column widths so the grid looks neat
            int cols = jTable.getColumnModel().getColumnCount();
            int w = 0;
            java.awt.Container parent = jTable.getParent();
            if (parent != null) {
                w = parent.getWidth();
            }
            if (w <= 0) {
                w = jTable.getWidth();
            }
            int per = (cols > 0 && w > 0) ? Math.max(70, w / cols) : 100;
            for (int i = 0; i < cols; i++) {
                jTable.getColumnModel().getColumn(i).setPreferredWidth(per);
            }
            jTable.doLayout();

            jTable.clearSelection();
            jTable.revalidate();
            jTable.repaint();

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Unable to load final grades: " + ex.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Loads courses into the combo box for the Student Grades view.
     */
    private void loadCoursesForGrades() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (int i = 0; i < courses.size(); i++) {
                jComboBox.addItem(courses.get(i));
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Fills the combo with available grade levels as "Grade X" strings for the
     * Final Grades view.
     */
    private void loadGradeLevelsForFinalGrades() {
        try {
            // We already have grade levels from StudentDAO
            List<Integer> grades = studentDAO.findGradeLevels();
            jComboBox.removeAllItems();
            for (int i = 0; i < grades.size(); i++) {
                Integer g = grades.get(i);
                jComboBox.addItem("Grade " + g);
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load grade levels: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads courses for the Manage Assignments screen.
     */
    private void loadCoursesForAssignments() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (int i = 0; i < courses.size(); i++) {
                jComboBox.addItem(courses.get(i));
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows the assignment list for the selected course with a hidden Delete
     * column (checkbox).
     */
    private void loadAssignmentsForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }

            List<Assignment> assignments = assignmentDAO.findByCourse(courseId);
            assignmentModel = new AssignmentTableModel(assignments);
            jTable.setModel(assignmentModel);

            // Sorter for this model
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(assignmentModel);
            jTable.setRowSorter(sorter);

            // Header left-align (only if default renderer supports it)
            java.awt.Component hdr = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdr instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

            // Left-aligned checkbox editor for the Delete column
            javax.swing.JCheckBox editorCheckBox = new javax.swing.JCheckBox();
            editorCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.setDefaultEditor(Boolean.class, new javax.swing.DefaultCellEditor(editorCheckBox));

            // Renderers: checkbox for Boolean, left renderer for others
            for (int i = 0; i < jTable.getColumnCount(); i++) {
                javax.swing.table.TableColumn col = jTable.getColumnModel().getColumn(i);
                if (jTable.getColumnClass(i) == Boolean.class) {
                    col.setCellRenderer(new javax.swing.table.TableCellRenderer() {
                        final javax.swing.JCheckBox cb = new javax.swing.JCheckBox();

                        {
                            cb.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                            cb.setOpaque(true);
                        }

                        @Override
                        public java.awt.Component getTableCellRendererComponent(
                                javax.swing.JTable table, Object value, boolean isSelected,
                                boolean hasFocus, int row, int column) {
                            cb.setSelected(Boolean.TRUE.equals(value));
                            if (isSelected) {
                                cb.setBackground(table.getSelectionBackground());
                                cb.setForeground(table.getSelectionForeground());
                            } else {
                                cb.setBackground(table.getBackground());
                                cb.setForeground(table.getForeground());
                            }
                            return cb;
                        }
                    });
                } else {
                    javax.swing.table.DefaultTableCellRenderer left = new javax.swing.table.DefaultTableCellRenderer();
                    left.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    col.setCellRenderer(left);
                }
            }

            // Show/hide the Delete column based on assignmentDeleteMode
            javax.swing.table.TableColumn[] ref = {assignmentDeleteColumn};
            applyDeleteVisibility(ref, assignmentDeleteMode);
            assignmentDeleteColumn = ref[0];

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load assignments: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads courses for the Attendance screen.
     */
    private void loadCoursesForAttendance() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (int i = 0; i < courses.size(); i++) {
                jComboBox.addItem(courses.get(i));
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows Monday–Friday attendance for the selected course; today's column is
     * highlighted in the UI.
     */
    private void loadAttendanceForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }

            List<Student> students = studentDAO.findByCourse(courseId);

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate start = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            java.time.LocalDate end = start.plusDays(4);

            AttendanceDAO dao = new UcanaccessAttendanceDAO();
            Map<Integer, Map<java.time.LocalDate, Boolean>> data
                    = dao.findByCourseAndDateRange(courseId, start, end);

            attendanceModel = new AttendanceTableModel(students, start, data);

            // Detach other modes
            studentTableModel = null;
            studentDeleteColumn = null;
            studentDeleteMode = false;
            studentSelectionModel = null;
            studentGradesModel = null;
            finalGradesModel = null;

            jTable.setModel(attendanceModel);

            // Sorter (by Student column)
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(attendanceModel);
            jTable.setRowSorter(sorter);

            java.util.List<javax.swing.RowSorter.SortKey> keys
                    = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
            keys.add(new javax.swing.RowSorter.SortKey(0, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();

            selectionMode = false;

            // Column index for today: 0 = Student, 1..5 = Mon..Fri
            int dow = today.getDayOfWeek().getValue(); // Mon=1..Sun=7
            attendanceTodayColumn = (dow >= 1 && dow <= 5) ? dow : -1;

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load attendance: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads courses for the Student Feedback screen.
     */
    private void loadCoursesForFeedback() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (int i = 0; i < courses.size(); i++) {
                jComboBox.addItem(courses.get(i));
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Shows the feedback note preview per student for the selected course.
     */
    private void loadFeedbackForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }

            List<Student> students = studentDAO.findByCourse(courseId);
            Map<Integer, String> notes = feedbackDAO.findByCourse(courseId);

            feedbackModel = new StudentFeedbackTableModel(students, notes);

            // Detach other modes
            studentTableModel = null;
            studentDeleteColumn = null;
            studentDeleteMode = false;
            studentSelectionModel = null;
            studentGradesModel = null;
            finalGradesModel = null;
            attendanceModel = null;

            jTable.setModel(feedbackModel);

            // Sorter (by Last Name)
            javax.swing.table.TableRowSorter<javax.swing.table.TableModel> sorter
                    = new javax.swing.table.TableRowSorter<javax.swing.table.TableModel>(feedbackModel);
            jTable.setRowSorter(sorter);

            java.util.List<javax.swing.RowSorter.SortKey> keys
                    = new java.util.ArrayList<javax.swing.RowSorter.SortKey>();
            keys.add(new javax.swing.RowSorter.SortKey(1, javax.swing.SortOrder.ASCENDING));
            sorter.setSortKeys(keys);
            sorter.sort();

            // Header left-align (safe)
            java.awt.Component hdr = jTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable, "", false, false, -1, -1);
            if (hdr instanceof javax.swing.table.DefaultTableCellRenderer) {
                ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                        .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load feedback: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * True if this TableColumn instance is currently in the view ColumnModel.
     */
    private static boolean isInView(javax.swing.table.TableColumnModel cm,
            javax.swing.table.TableColumn col) {
        if (col == null) {
            return false;
        }
        for (int i = 0; i < cm.getColumnCount(); i++) {
            if (cm.getColumn(i) == col) {
                return true;
            }
        }
        return false;
    }

    /**
     * Show/hide a cached delete column. If showing, keep it at view index 0.
     */
    private void applyDeleteVisibility(javax.swing.table.TableColumn[] cacheRef, boolean show) {
        javax.swing.table.TableColumnModel cm = jTable.getColumnModel();
        // Cache the *current model's* first column as the delete column if needed
        if (cacheRef[0] == null && cm.getColumnCount() > 0) {
            cacheRef[0] = cm.getColumn(0);
        }
        javax.swing.table.TableColumn col = cacheRef[0];
        if (col == null) {
            return;
        }

        boolean present = isInView(cm, col);

        if (show) {
            if (!present) {
                jTable.addColumn(col);
                present = true;
            }
            // ensure it's at index 0
            if (present) {
                int last = cm.getColumnCount() - 1;
                int from = -1;
                for (int i = 0; i < cm.getColumnCount(); i++) {
                    if (cm.getColumn(i) == col) {
                        from = i;
                        break;
                    }
                }
                if (from >= 0 && from != 0) {
                    cm.moveColumn(from, 0);
                }
            }
        } else {
            if (present) {
                jTable.removeColumn(col);
            }
        }
    }

    /**
     * Rebuild the current grid according to the active model + flags.
     */
    private void reloadCurrentGrid() {
        if (finalGradesModel != null) {
            loadFinalGradesForSelectedGrade();
            return;
        }
        if (attendanceModel != null) {
            loadAttendanceForSelectedCourse();
            return;
        }
        if (feedbackModel != null) {
            loadFeedbackForSelectedCourse();
            return;
        }
        if (assignmentModel != null) {
            loadAssignmentsForSelectedCourse();
            return;
        }
        if (courseModel != null) {
            loadCoursesForSelectedGrade();
            return;
        }
        if (studentGradesModel != null) {
            loadStudentGradesForSelectedCourse();
            return;
        }
        /* default */ loadStudentsForSelectedCourse();
    }

}
