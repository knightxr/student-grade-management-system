package sgms.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JTextField;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;
import sgms.dao.AssignmentDAO;
import sgms.dao.FinalGradeDAO;
import sgms.dao.GradeDAO;
import sgms.dao.StudentDAO;
import sgms.dao.AttendanceDAO;
import sgms.dao.FeedbackDAO;
import sgms.dao.CourseDAO;
import sgms.dao.impl.UcanaccessAssignmentDAO;
import sgms.dao.impl.UcanaccessFinalGradeDAO;
import sgms.dao.impl.UcanaccessGradeDAO;
import sgms.dao.impl.UcanaccessStudentDAO;
import sgms.dao.impl.UcanaccessAttendanceDAO;
import sgms.dao.impl.UcanaccessFeedbackDAO;
import sgms.dao.impl.UcanaccessCourseDAO;
import sgms.model.Assignment;
import sgms.model.Course;
import sgms.model.FinalGrade;
import sgms.model.Student;
import sgms.util.SearchUtil;
import sgms.util.ReportCardGenerator;
import sgms.ui.AssignmentTableModel;

/**
 * 
 * @author Jacques Smit 12E
 *  Main application frame that hosts the navigation sidebar and a data table
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
    private javax.swing.table.TableColumn courseDeleteColumn;
    private javax.swing.table.TableColumn assignmentDeleteColumn;
    private boolean courseDeleteMode = false;
    private boolean assignmentDeleteMode = false;
    private boolean selectionMode = false;
    private int attendanceTodayColumn = -1;
    private final FeedbackDAO feedbackDAO = new UcanaccessFeedbackDAO();
    private final AssignmentDAO assignmentDAO = new UcanaccessAssignmentDAO();
    private final GradeDAO gradeDAO = new UcanaccessGradeDAO();
    private final AttendanceDAO attendanceDAO = new UcanaccessAttendanceDAO();
    private javax.swing.JButton lastActionButton;
    
    private void setActiveButton(javax.swing.JButton active) {
        Color defaultColor = Color.WHITE;
        Color activeColor = new Color(0, 102, 204);
        javax.swing.JButton[] buttons = {
            jButtonViewStudents,
            jButtonViewStudentGrades,
            jButtonViewFinalGrades,
            jButtonAttendance,
            jButtonStudentFeedback,
            jButtonManageCourses,
            jButtonManageAssignments
        };
        for (javax.swing.JButton b : buttons) {
            if (b != null) {
                b.setBackground(b == active ? activeColor : defaultColor);
                b.setOpaque(true);
                b.setBorderPainted(false);
            }
        }
    }
    
    private void installContextTracking() {
        java.awt.event.ActionListener tracker = e -> {
            Object src = e.getSource();
            if (src instanceof javax.swing.JButton btn && btn != jButtonHelp) {
                lastActionButton = btn;
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
        for (javax.swing.JButton b : buttons) {
            if (b != null) {
                b.addActionListener(tracker);
            }
        }
    }
    /**
     * Creates new form MainFrame
     */
    public MainPage() {
        initComponents();
        setResizable(false);
        jTable = new JTable() {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                Color base = (row % 2 == 0) ? Color.WHITE : new Color(235, 235, 235);
                boolean colored = false;

                if (selectionMode && MainPage.this.studentSelectionModel != null) {
                    int modelRow = convertRowIndexToModel(row);
                    if (MainPage.this.studentSelectionModel.isNewlySelected(modelRow)) {
                        c.setBackground(Color.GREEN);
                        colored = true;
                    } else if (MainPage.this.studentSelectionModel.isDeselected(modelRow)) {
                        c.setBackground(Color.RED);
                        colored = true;
                    }
                } else {
                    if (attendanceModel != null && column == attendanceTodayColumn && !isRowSelected(row)) {
                        c.setBackground(new Color(255, 255, 200));
                        colored = true;
                    }
                    if (courseModel != null) {
                        int modelRow = convertRowIndexToModel(row);
                        if (courseModel.isMarkedForDeletion(modelRow) && !isRowSelected(row)) {
                            c.setBackground(Color.RED);
                            colored = true;
                        }
                    } else if (assignmentModel != null) {
                        int modelRow = convertRowIndexToModel(row);
                        if (assignmentModel.isMarkedForDeletion(modelRow) && !isRowSelected(row)) {
                            c.setBackground(Color.RED);
                            colored = true;
                        }
                    }
                }

                if (!colored && !isRowSelected(row)) {
                    c.setBackground(base);
                }

                if (c instanceof javax.swing.JComponent comp) {
                    comp.setOpaque(true);
                }

                return c;
            }
        };
        jScrollPane.setViewportView(jTable);
        jTable.setModel(new javax.swing.table.DefaultTableModel());
        jTable.setAutoCreateRowSorter(true);
        jTable.getTableHeader().setFont(jTable.getTableHeader().getFont().deriveFont(Font.BOLD));
        jTextFieldSearch.setText("Search");
        jTextFieldSearch.setForeground(Color.GRAY);
        jButtonSave.addActionListener(this::jButtonSaveActionPerformed);
        jButtonEdit.addActionListener(this::jButtonEditActionPerformed);
        SearchUtil.installSearch(jTable, jTextFieldSearch, jButtonSearch);
        jComboBox.setEnabled(false);
        jComboBox.removeAllItems();
        setActiveButton(null);
        
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
                            JOptionPane.showMessageDialog(MainPage.this, full, "Comment", JOptionPane.INFORMATION_MESSAGE);
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
        setActiveButton(jButtonViewStudentGrades);
        loadCoursesForGrades();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);
        studentTableModel = null;
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
        loadStudentGradesForSelectedCourse();
    }//GEN-LAST:event_jButtonViewStudentGradesActionPerformed

    private void jButtonManageCoursesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManageCoursesActionPerformed
        setActiveButton(jButtonManageCourses);
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
        studentTableModel = null;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        selectionMode = false;
        attendanceTodayColumn = -1;
        loadCoursesForSelectedGrade();
    }//GEN-LAST:event_jButtonManageCoursesActionPerformed

    private void jButtonViewStudentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewStudentsActionPerformed
        setActiveButton(jButtonViewStudents);
        loadCourses();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        attendanceTodayColumn = -1;
        selectionMode = false;
        studentSelectionModel = null;
        loadStudentsForSelectedCourse();
    }//GEN-LAST:event_jButtonViewStudentsActionPerformed

    private void jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddActionPerformed
        if (finalGradesModel != null) {
            return;
        }
        if (courseModel != null) {
            int grade = getSelectedGradeLevel();
            String code = JOptionPane.showInputDialog(this, "Enter course code:");
            if (code == null || code.trim().isEmpty()) {
                return;
            }
            String name = JOptionPane.showInputDialog(this, "Enter course name:");
            if (name == null || name.trim().isEmpty()) {
                return;
            }
            Course c = new Course(0, code.trim(), name.trim(), grade, 0);
            if (jTable.getRowSorter() != null) {
                jTable.getRowSorter().setSortKeys(null);
            }
            courseModel.addCourse(c);
            int row = courseModel.getRowCount() - 1;
            jTable.setRowSelectionInterval(row, row);
            jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));
            return;
        }
        if (assignmentModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }
            JTextField titleField = new JTextField();
            JTextField maxField = new JTextField();
            JTextField termField = new JTextField();
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            JPanel panel = new JPanel(new GridLayout(0, 2));
            panel.add(new JLabel("Title:"));
            panel.add(titleField);
            panel.add(new JLabel("Max Marks:"));
            panel.add(maxField);
            panel.add(new JLabel("Term:"));
            panel.add(termField);
            panel.add(new JLabel("Due Date:"));
            panel.add(dateSpinner);
            int result = JOptionPane.showConfirmDialog(this, panel, "New Assignment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title is required.");
                    return;
                }
                String maxText = maxField.getText().trim();
                if (maxText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Max marks are required.");
                    return;
                }
                int max;
                try {
                    max = Integer.parseInt(maxText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Max marks must be a number.");
                    return;
                }
                String termText = termField.getText().trim();
                if (termText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Term is required.");
                    return;
                }
                int term;
                try {
                    term = Integer.parseInt(termText);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Term must be a number between 1 and 4.");
                    return;
                }
                if (term < 1 || term > 4) {
                    JOptionPane.showMessageDialog(this, "Term must be between 1 and 4.");
                    return;
                }
                java.util.Date utilDate = (java.util.Date) dateSpinner.getValue();
                java.sql.Date due = new java.sql.Date(utilDate.getTime());
                Assignment a = new Assignment(0, courseId, title, max, term, due);
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
        if (feedbackModel != null) {
            int row = jTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a student first.");
                return;
            }
            int modelRow = jTable.convertRowIndexToModel(row);
            Student s = feedbackModel.getStudent(modelRow);
            String comment = JOptionPane.showInputDialog(this, "Enter comment for " + s.getFirstName() + " " + s.getLastName() + ":");
            if (comment != null) {
                try {
                    feedbackDAO.upsert(s.getStudentId(), getSelectedCourseId(), comment);
                    loadFeedbackForSelectedCourse();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Unable to add feedback: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }
        if (studentGradesModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId > 00 & !selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }
        int courseId = getSelectedCourseId();
        if (courseId > 0) {
            if (!selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }
        if (studentTableModel != null) {
            try {
                Student s = studentDAO.add(new Student("", "", 0));
                if (jTable.getRowSorter() != null) {
                    jTable.getRowSorter().setSortKeys(null);
                }
                studentTableModel.addStudent(s);
                int row = studentTableModel.getRowCount() - 1;
                jTable.setRowSelectionInterval(row, row);
                jTable.scrollRectToVisible(jTable.getCellRect(row, 0, true));
                jTable.editCellAt(row, 1);
                Component editor = jTable.getEditorComponent();
                if (editor != null) {
                    editor.requestFocusInWindow();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to add student: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonAddActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        if (finalGradesModel != null) {
            return;
        }
        if (courseModel != null) {
            if (!courseDeleteMode) {
                jTable.addColumn(courseDeleteColumn);
                jTable.moveColumn(jTable.getColumnCount() - 1, 0);
                courseDeleteMode = true;
            } else {
                int row = jTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = jTable.convertRowIndexToModel(row);
                    courseModel.markDeleted(modelRow);
                    jTable.repaint();
                }
            }
            return;
        }
        if (assignmentModel != null) {
            if (!assignmentDeleteMode) {
                jTable.addColumn(assignmentDeleteColumn);
                jTable.moveColumn(jTable.getColumnCount() - 1, 0);
                assignmentDeleteMode = true;
            } else {
                int row = jTable.getSelectedRow();
                if (row >= 0) {
                    int modelRow = jTable.convertRowIndexToModel(row);
                    assignmentModel.markDeleted(modelRow);
                    jTable.repaint();
                }
            }
            return;
        }
        if (feedbackModel != null) {
            int row = jTable.getSelectedRow();
            if (row >= 0) {
                int modelRow = jTable.convertRowIndexToModel(row);
                Student s = feedbackModel.getStudent(modelRow);
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Delete comment for " + s.getFirstName() + " " + s.getLastName() + "?",
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        feedbackDAO.delete(s.getStudentId(), getSelectedCourseId());
                        loadFeedbackForSelectedCourse();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Unable to delete feedback: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            return;
        }
        if (studentGradesModel != null) {
            int courseId = getSelectedCourseId();
            if (courseId > 0 && !selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }
        if (studentTableModel == null) {
            return;
        }
        int courseId = getSelectedCourseId();
        if (courseId > 0) {
            if (!selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }
        int row = jTable.getSelectedRow();
        if (row >= 0) {
            Student s = studentTableModel.getStudent(row);
            if (s.getStudentId() > 0) {
                try {
                    studentDAO.delete(s.getStudentId());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Unable to delete student: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            studentTableModel.removeStudent(row);
        }
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
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldSearchActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        String message;
        if (lastActionButton == null) {
            message = "Press a button first to get contextual help. "
                    + "The table supports sorting by clicking column headers and filtering with the Search box.";
        } else if (lastActionButton == jButtonViewStudents) {
            message = "View Students shows all learners in the selected course.\n"
                    + "Use the table to sort columns, filter with the Search box and select rows to edit or delete.";
        } else if (lastActionButton == jButtonViewStudentGrades) {
            message = "View Student Grades lists assignment marks per student.\n"
                    + "Sort columns to compare results, use Search to find a student and double-click a grade cell to edit when editing is enabled.";
        } else if (lastActionButton == jButtonViewFinalGrades) {
            message = "View Final Grades displays overall grades for each student.\n"
                    + "Columns can be sorted and filtered just like other tables.";
        } else if (lastActionButton == jButtonAttendance) {
            message = "Attendance lets you mark daily presence.\n"
                    + "The yellow column is today—click cells to toggle present or absent, then press Save to store changes.";
        } else if (lastActionButton == jButtonStudentFeedback) {
            message = "Student Feedback allows adding or editing comments.\n"
                    + "Click a comment cell to view the full text and use the table tools to sort or search.";
        } else if (lastActionButton == jButtonManageCourses) {
            message = "Manage Courses creates, edits or deletes courses.\n"
                    + "Rows marked in red are flagged for deletion until you Save.";
        } else if (lastActionButton == jButtonManageAssignments) {
            message = "Manage Assignments works like courses but for assignments.\n"
                    + "Use Add to create new rows and Save to commit changes.";
        } else if (lastActionButton == jButtonCreateReportCard) {
            message = "Create Report Card generates a PDF for the selected student.\n"
                    + "Select a student row first, then click this button.";
        } else if (lastActionButton == jButtonAdd) {
            message = "Add inserts a new blank row into the current table so you can capture a new record.";
        } else if (lastActionButton == jButtonDelete) {
            message = "Delete removes the selected record or marks it in red for deletion until you Save.";
        } else if (lastActionButton == jButtonSave) {
            message = "Save commits all table edits and deletions to the database.";
        } else if (lastActionButton == jButtonEdit) {
            message = "Edit unlocks the selected row so its cells can be modified.";
        } else if (lastActionButton == jButtonSearch) {
            message = "Search filters the table according to the text entered in the Search box.\n"
                    + "Click column headers to sort the results.";
        } else {
            message = "Press a button first to get contextual help. "
                    + "The table supports sorting by clicking column headers and filtering with the Search box.";
        }
        JOptionPane.showMessageDialog(this, message, "Help", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jButtonLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLogoutActionPerformed
        LoginPage loginPage = new LoginPage();
        loginPage.setVisible(true);
        dispose();

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
        if (selectionMode && studentSelectionModel != null) {
            try {
                int courseId = getSelectedCourseId();
                Set<Integer> selected = studentSelectionModel.getSelectedStudentIds();
                Set<Integer> original = studentSelectionModel.getOriginallySelectedIds();
                for (Integer id : selected) {
                    if (!original.contains(id)) {
                        studentDAO.enrollStudentInCourse(id, courseId);
                    }
                }
                for (Integer id : original) {
                    if (!selected.contains(id)) {
                        studentDAO.removeStudentFromCourse(id, courseId);
                    }
                }
                selectionMode = false;
                studentSelectionModel = null;
                attendanceModel = null;
                attendanceTodayColumn = -1;
                loadStudentsForSelectedCourse();
                setActiveButton(jButtonViewStudents);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to update enrollments: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (assignmentModel != null) {
            try {
                Set<Integer> deleted = assignmentModel.getDeletedIds();
                for (Assignment a : assignmentModel.getAssignments()) {
                    if (deleted.contains(a.getAssignmentId())) {
                        if (a.getAssignmentId() > 0) {
                            assignmentDAO.delete(a.getAssignmentId());
                        }
                    } else {
                        if (a.getAssignmentId() == 0) {
                            assignmentDAO.add(a);
                        } else {
                            assignmentDAO.update(a);
                        }
                    }
                }
                loadAssignmentsForSelectedCourse();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save assignments: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (courseModel != null) {
            try {
                Set<Integer> deleted = courseModel.getDeletedIds();
                for (Course c : courseModel.getCourses()) {
                    if (deleted.contains(c.getCourseId())) {
                        if (c.getCourseId() > 0) {
                            courseDAO.delete(c.getCourseId());
                        }
                    } else {
                        if (c.getCourseId() == 0) {
                            courseDAO.add(c);
                        } else {
                            courseDAO.update(c);
                        }
                    }
                }
                loadCoursesForSelectedGrade();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save courses: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (attendanceModel != null) {
            try {
                AttendanceDAO dao = new UcanaccessAttendanceDAO();
                int courseId = getSelectedCourseId();
                for (Map.Entry<Integer, Map<LocalDate, Boolean>> e : attendanceModel.getChanges().entrySet()) {
                    int studentId = e.getKey();
                    for (Map.Entry<LocalDate, Boolean> att : e.getValue().entrySet()) {
                        dao.upsert(studentId, courseId, att.getKey(), att.getValue());
                    }
                }
                attendanceModel.clearChanges();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save attendance: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
         if (feedbackModel != null) {
            try {
                int courseId = getSelectedCourseId();
                for (Map.Entry<Integer, String> e : feedbackModel.getComments().entrySet()) {
                    int studentId = e.getKey();
                    String note = e.getValue();
                    if (note == null || note.trim().isEmpty()) {
                        feedbackDAO.delete(studentId, courseId);
                    } else {
                        feedbackDAO.upsert(studentId, courseId, note);
                    }
                }
                loadFeedbackForSelectedCourse();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save feedback: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (studentTableModel == null) {
            return;
        }
        for (Student s : studentTableModel.getStudents()) {
            if (s.getFirstName().trim().isEmpty() || s.getLastName().trim().isEmpty() || s.getGradeLevel() <= 0) {
                JOptionPane.showMessageDialog(this,
                        "All student fields must be filled.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        try {
            for (Student s : studentTableModel.getStudents()) {
                if (s.getStudentId() == 0) {
                    studentDAO.add(s);
                } else {
                    studentDAO.update(s);
                }
            }
            // Refresh table to show any generated IDs
            int courseId = getSelectedCourseId();
            List<Student> students = courseId > 0 ?
                    studentDAO.findByCourse(courseId) : studentDAO.findAll();
            studentTableModel.setStudents(students);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save students: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonSaveActionPerformed

    private void jButtonEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditActionPerformed
        if (finalGradesModel != null) {
            return;
        }
        if (courseModel != null) {
            if (jTable.getRowCount() > 0) {
                jTable.changeSelection(0, 2, false, false);
                jTable.editCellAt(0, 2);
                Component editor = jTable.getEditorComponent();
                if (editor instanceof JTextComponent tc) {
                    tc.requestFocusInWindow();
                    tc.selectAll();
                }
            }
            return;
        }
        if (assignmentModel != null) {
            if (jTable.getRowCount() > 0) {
                jTable.changeSelection(0, 2, false, false);
                jTable.editCellAt(0, 2);
                Component editor = jTable.getEditorComponent();
                if (editor instanceof JTextComponent tc) {
                    tc.requestFocusInWindow();
                    tc.selectAll();
                }
            }
            return;
        }
        if (feedbackModel != null) {
            if (jTable.getRowCount() > 0) {
                jTable.changeSelection(0, 2, false, false);
                jTable.editCellAt(0, 2);
                Component editor = jTable.getEditorComponent();
                if (editor instanceof JTextComponent tc) {
                    tc.requestFocusInWindow();
                    tc.selectAll();
                }
            }
            return;
        }
        if (studentGradesModel != null) {
            if (jTable.getRowCount() > 0 && jTable.getColumnCount() > 1) {
                jTable.changeSelection(0, 1, false, false);
                jTable.editCellAt(0, 1);
                Component editor = jTable.getEditorComponent();
                if (editor instanceof JTextComponent tc) {
                    tc.requestFocusInWindow();
                    tc.selectAll();
                }
            }
            return;
        }
        if (studentTableModel == null) {
            return;
        }
        int courseId = getSelectedCourseId();
        if (courseId > 0) {
            if (!selectionMode) {
                startEnrollmentEdit();
            }
            return;
        }
        int row = jTable.getSelectedRow();
        int col = jTable.getSelectedColumn();
        if (row >= 0 && col >= 0) {
            jTable.editCellAt(row, col);
        }
    }//GEN-LAST:event_jButtonEditActionPerformed

    private void jButtonSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonSearchActionPerformed

    private void jButtonViewFinalGradesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewFinalGradesActionPerformed
        setActiveButton(jButtonViewFinalGrades);
        loadGradeLevelsForFinalGrades();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(false);
        jButtonDelete.setEnabled(false);
        jButtonSave.setEnabled(false);
        jButtonEdit.setEnabled(false);
        studentTableModel = null;
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
        loadFinalGradesForSelectedGrade();
    }//GEN-LAST:event_jButtonViewFinalGradesActionPerformed

    private void jButtonAttendanceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAttendanceActionPerformed
        setActiveButton(jButtonAttendance);
        loadCoursesForAttendance();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(false);
        jButtonDelete.setEnabled(false);
        jButtonEdit.setEnabled(false);
        jButtonSave.setEnabled(true);
        studentTableModel = null;
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
        loadAttendanceForSelectedCourse();
    }//GEN-LAST:event_jButtonAttendanceActionPerformed

    private void jButtonStudentFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStudentFeedbackActionPerformed
        setActiveButton(jButtonStudentFeedback);
        loadCoursesForFeedback();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonEdit.setEnabled(true);
        jButtonSave.setEnabled(true);
        studentTableModel = null;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        selectionMode = false;
        loadFeedbackForSelectedCourse();
    }//GEN-LAST:event_jButtonStudentFeedbackActionPerformed

    private void jButtonManageAssignmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManageAssignmentsActionPerformed
        setActiveButton(jButtonManageAssignments);
        loadCoursesForAssignments();
        jComboBox.setEnabled(true);
        jButtonAdd.setEnabled(true);
        jButtonDelete.setEnabled(true);
        jButtonSave.setEnabled(true);
        jButtonEdit.setEnabled(true);
        studentTableModel = null;
        studentSelectionModel = null;
        studentGradesModel = null;
        finalGradesModel = null;
        attendanceModel = null;
        feedbackModel = null;
        assignmentModel = null;
        assignmentDeleteColumn = null;
        assignmentDeleteMode = false;
        courseModel = null;
        courseDeleteColumn = null;
        courseDeleteMode = false;
        selectionMode = false;
        attendanceTodayColumn = -1;
        loadAssignmentsForSelectedCourse();
    }//GEN-LAST:event_jButtonManageAssignmentsActionPerformed

    private void jButtonCreateReportCardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateReportCardActionPerformed
        Student selected = getSelectedStudent();
        if (selected == null) {
            return;
        }
        jButtonCreateReportCard.setEnabled(false);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Map<String, String> data = ReportCardGenerator.buildData(selected,
                        courseDAO, assignmentDAO, gradeDAO, feedbackDAO);
                Path docx = ReportCardGenerator.generateDocx(data);
                ReportCardGenerator.openFile(docx);
                return null;
            }

            @Override
            protected void done() {
                jButtonCreateReportCard.setEnabled(true);
                try {
                    get();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainPage.this,
                            "Unable to create report card: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
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

private void loadCourses() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            jComboBox.addItem(new Course(0, "All Students"));
            for (Course c : courses) {
                jComboBox.addItem(c);
            }
            jComboBox.setSelectedIndex(0);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedCourseId() {
        Object sel = jComboBox.getSelectedItem();
        if (sel instanceof Course c) {
            return c.getCourseId();
        }
        return 0;
    }

    private int getSelectedGradeLevel() {
        Object sel = jComboBox.getSelectedItem();
        if (sel instanceof Integer g) {
            return g;
        }
        if (sel instanceof String s ) {
            try {
                return Integer.parseInt(s.replaceAll("\\D", ""));
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
        return 0;
    }
    
    private Student getSelectedStudent() {
        if (studentTableModel == null) {
            JOptionPane.showMessageDialog(this, "Please view students and select a record first.");
            return null;
        }
        int viewRow = jTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table.");
            return null;
        }
        int modelRow = jTable.convertRowIndexToModel(viewRow);
        return studentTableModel.getStudent(modelRow);
    }
    
    private void loadCoursesForSelectedGrade() {
        try {
            int grade = getSelectedGradeLevel();
            List<Course> courses = courseDAO.findByGrade(grade);
            courseModel = new CourseTableModel(courses);
            jTable.setModel(courseModel);
            jTable.setRowSorter(new TableRowSorter<>(courseModel));
            ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                    .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
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
                        }
                        @Override
                        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            cb.setSelected(Boolean.TRUE.equals(value));
                            return cb;
                        }
                    });
                } else {
                    javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer();
                    leftRenderer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    column.setCellRenderer(leftRenderer);
                }
            }
            courseDeleteColumn = jTable.getColumnModel().getColumn(0);
            jTable.removeColumn(courseDeleteColumn);
            courseDeleteMode = false;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadStudentsForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            List<Student> students = courseId > 0 ?
                    studentDAO.findByCourse(courseId) : studentDAO.findAll();
            studentTableModel = new StudentTableModel(students);
            jTable.setModel(studentTableModel);
            jTable.setAutoCreateRowSorter(true);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(new RowSorter.SortKey(2, SortOrder.ASCENDING)));
            selectionMode = false;
            studentSelectionModel = null;
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load students: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startEnrollmentEdit() {
        try {
            int courseId = getSelectedCourseId();
            List<Student> all = studentDAO.findAll();
            List<Student> enrolled = studentDAO.findByCourse(courseId);
            Set<Integer> initial = new HashSet<>();
            for (Student s : enrolled) {
                initial.add(s.getStudentId());
            }
            studentSelectionModel = new StudentSelectionTableModel(all, initial);
            jTable.setModel(studentSelectionModel);
            jTable.setAutoCreateRowSorter(true);
            javax.swing.table.DefaultTableCellRenderer leftRenderer =
                    new javax.swing.table.DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jTable.getColumnModel().getColumn(1).setCellRenderer(leftRenderer);
            jTable.getColumnModel().getColumn(4).setCellRenderer(leftRenderer);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(
                    new RowSorter.SortKey(4, SortOrder.ASCENDING),
                    new RowSorter.SortKey(3, SortOrder.ASCENDING),
                    new RowSorter.SortKey(2, SortOrder.ASCENDING)));
            selectionMode = true;
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
            setActiveButton(jButtonViewStudents);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load students: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
  private void loadStudentGradesForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }
            GradeDAO gradeDAO = new UcanaccessGradeDAO();
            List<Student> students = studentDAO.findByCourse(courseId);
            List<Assignment> assignments = assignmentDAO.findByCourse(courseId);
            Map<Integer, Map<Integer, Integer>> grades = gradeDAO.findByCourse(courseId);
            studentGradesModel = new StudentGradesTableModel(students, assignments, grades, gradeDAO);
            finalGradesModel = null;
            studentTableModel = null;
            selectionMode = false;
            studentSelectionModel = null;
            jTable.setModel(studentGradesModel);
            jTable.setAutoCreateRowSorter(true);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load grades: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFinalGradesForSelectedGrade() {
        try {
            int grade = getSelectedGradeLevel();
            if (grade <= 0) {
                return;
            }
            FinalGradeDAO dao = new UcanaccessFinalGradeDAO();
            List<FinalGrade> list = dao.findByGradeLevel(grade);
            finalGradesModel = new FinalGradesTableModel(list);
            studentGradesModel = null;
            studentTableModel = null;
            selectionMode = false;
            studentSelectionModel = null;
            jTable.setModel(finalGradesModel);
            jTable.setAutoCreateRowSorter(true);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load final grades: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCoursesForGrades() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (Course c : courses) {
                jComboBox.addItem(c);
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadGradeLevelsForFinalGrades() {
        try {
            FinalGradeDAO dao = new UcanaccessFinalGradeDAO();
            List<Integer> grades = studentDAO.findGradeLevels();
            jComboBox.removeAllItems();
            for (Integer g : grades) {
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
  
    private void loadCoursesForAssignments() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (Course c : courses) {
                jComboBox.addItem(c);
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAssignmentsForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }
            List<Assignment> assignments = assignmentDAO.findByCourse(courseId);
            assignmentModel = new AssignmentTableModel(assignments);
            jTable.setModel(assignmentModel);
            jTable.setRowSorter(new TableRowSorter<>(assignmentModel));
            ((javax.swing.table.DefaultTableCellRenderer) jTable.getTableHeader().getDefaultRenderer())
                    .setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
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
                        }
                        @Override
                        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            cb.setSelected(Boolean.TRUE.equals(value));
                            return cb;
                        }
                    });
                } else {
                    javax.swing.table.DefaultTableCellRenderer leftRenderer = new javax.swing.table.DefaultTableCellRenderer();
                    leftRenderer.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                    column.setCellRenderer(leftRenderer);
                }
            }
            assignmentDeleteColumn = jTable.getColumnModel().getColumn(0);
            jTable.removeColumn(assignmentDeleteColumn);
            assignmentDeleteMode = false;
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load assignments: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCoursesForAttendance() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (Course c : courses) {
                jComboBox.addItem(c);
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }
            List<Student> students = studentDAO.findByCourse(courseId);
            LocalDate today = LocalDate.now();
            LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate end = start.plusDays(4);
            AttendanceDAO dao = new UcanaccessAttendanceDAO();
            Map<Integer, Map<LocalDate, Boolean>> data = dao.findByCourseAndDateRange(courseId, start, end);
            attendanceModel = new AttendanceTableModel(students, start, data);
            studentTableModel = null;
            studentSelectionModel = null;
            studentGradesModel = null;
            finalGradesModel = null;
            jTable.setModel(attendanceModel);
            jTable.setAutoCreateRowSorter(true);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
            selectionMode = false;
            attendanceTodayColumn = today.getDayOfWeek().getValue();
            if (attendanceTodayColumn < 1 || attendanceTodayColumn > 5) {
                attendanceTodayColumn = -1;
            }
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load attendance: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadCoursesForFeedback() {
        try {
            List<Course> courses = studentDAO.findCourses();
            jComboBox.removeAllItems();
            for (Course c : courses) {
                jComboBox.addItem(c);
            }
            if (jComboBox.getItemCount() > 0) {
                jComboBox.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load courses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFeedbackForSelectedCourse() {
        try {
            int courseId = getSelectedCourseId();
            if (courseId <= 0) {
                return;
            }
            List<Student> students = studentDAO.findByCourse(courseId);
            Map<Integer, String> notes = feedbackDAO.findByCourse(courseId);
            feedbackModel = new StudentFeedbackTableModel(students, notes);
            studentTableModel = null;
            studentSelectionModel = null;
            studentGradesModel = null;
            finalGradesModel = null;
            attendanceModel = null;
            jTable.setModel(feedbackModel);
            jTable.setAutoCreateRowSorter(true);
            TableRowSorter<?> sorter = (TableRowSorter<?>) jTable.getRowSorter();
            sorter.setSortKeys(List.of(new RowSorter.SortKey(1, SortOrder.ASCENDING)));
            SearchUtil.applyFilter(jTable, jTextFieldSearch.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load feedback: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
