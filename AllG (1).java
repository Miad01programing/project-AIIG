package allg;

import java.sql.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.awt.Desktop;
import java.awt.event.*;

import java.util.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;

import java.net.URI;

class DBConnection {
    public static Connection connect() {
        try {
            String password = "app"; 

            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}

public class AllG {
    public static void main(String[] args) {
        new LibraryWelcomeGUI();
        
        Connection conn = DBConnection.connect();
        
        if (conn != null) {
        }
    }
}

class LibraryWelcomeGUI extends JFrame {

    public LibraryWelcomeGUI() {
        setTitle("Library System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(239, 235, 223));

        ImageIcon iau = new ImageIcon("IAU50.png");
        Image img = iau.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel lp1 = new JLabel(new ImageIcon(img));
        lp1.setBounds(10, 10, 80, 80);
        panel.add(lp1);

        ImageIcon centerImageIcon = new ImageIcon("BOOK.JPG");
        Image centerImg = centerImageIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel centerLabel = new JLabel(new ImageIcon(centerImg));
        centerLabel.setBounds(250, 40, 100, 100);
        panel.add(centerLabel);

        JLabel titleLabel = new JLabel("Welcome to the University Library");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setBounds(150, 150, 300, 30);
        panel.add(titleLabel);

        JButton continueButton = new JButton("Continue");
        continueButton.setBounds(250, 200, 100, 30);
        panel.add(continueButton);

        add(panel);

        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUserSelection();
            }
        });

        setVisible(true);
    }

    private void showUserSelection() {
        String[] options = {"Student", "Teacher"};
        int choice = JOptionPane.showOptionDialog(
            this, "Are you a Student or a Teacher?", "User Selection",
            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]
        );

        if (choice == 0) {
            new LevelSelectionGUI(this);
            setVisible(false);
        } else if (choice == 1) {
            new TeacherGUI();
        }
        dispose();
    }
}

//---------------------------------------------------------------------------

class LevelSelectionGUI extends JFrame {
    private JTextField universityIDField;
    private JComboBox<Integer> levelBox;
    private JButton nextButton;

    public LevelSelectionGUI(JFrame previousFrame) {
        this.setTitle("Level Selection GUI");
        this.setSize(350, 220);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(239,235,223));

        JLabel idLabel = new JLabel("Enter your University ID:");
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idLabel);

        universityIDField = new JTextField(15);
        universityIDField.setMaximumSize(new Dimension(200, 25));
        universityIDField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(universityIDField);
        panel.add(Box.createVerticalStrut(10));

        JLabel levelLabel = new JLabel("Choose your level:");
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(levelLabel);

        levelBox = new JComboBox<>(new Integer[]{1, 2});
        levelBox.setMaximumSize(new Dimension(200, 25));
        levelBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(levelBox);
        panel.add(Box.createVerticalStrut(10));

        nextButton = new JButton("Next");
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(e -> openResourceSelection());
        panel.add(nextButton);

        this.add(panel);
        this.setVisible(true);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            previousFrame.setVisible(true);
            this.dispose();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(backButton);
    }

    private void openResourceSelection() {
        String universityID = universityIDField.getText();

        if (!universityID.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "Invalid University ID! Must contain 10 digits.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!checkIDInDatabase(universityID)) {
            JOptionPane.showMessageDialog(this, "ID not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int level = (int) levelBox.getSelectedItem();
        this.setVisible(false);
        new ResourceSelectionGUI(this, universityID, level);
    }

    private boolean checkIDInDatabase(String universityID) {
        boolean exists = false;
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM STUDENT WHERE S_ID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, Integer.parseInt(universityID));
            ResultSet rs = stmt.executeQuery();
            exists = rs.next();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return exists;
    }
}


class ResourceSelectionGUI extends JFrame {
    private JComboBox<String> courseBox, resourceBox;
    private JButton submitButton;
    private ResourceManager resourceManager;
    private JFrame previousFrame;
    private int level;

    public ResourceSelectionGUI(JFrame previousFrame, String universityID, int level) {
        super("Select Resources");
        this.previousFrame = previousFrame;
        this.level = level;

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(239, 235, 223));

        JLabel idLabel = new JLabel("University ID: " + universityID);
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idLabel);

        JLabel levelLabel = new JLabel("Level: " + level);
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(levelLabel);
        panel.add(Box.createVerticalStrut(10));

        JLabel courseLabel = new JLabel("Choose a course:");
        courseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(courseLabel);

        courseBox = new JComboBox<>();
        courseBox.setMaximumSize(new Dimension(200, 25));
        courseBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(courseBox);
        panel.add(Box.createVerticalStrut(10));

        JLabel resourceLabel = new JLabel("Choose resource type:");
        resourceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resourceLabel);

        resourceBox = new JComboBox<>(new String[]{"Video", "Books"});
        resourceBox.setMaximumSize(new Dimension(200, 25));
        resourceBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(resourceBox);
        panel.add(Box.createVerticalStrut(10));

        submitButton = new JButton("Submit");
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.addActionListener(e -> handleSubmit());
        panel.add(submitButton);
        
        JButton viewTeacherResourcesButton = new JButton("View Teacher Resources");
        viewTeacherResourcesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewTeacherResourcesButton.addActionListener(e -> handleViewTeacherResources());
        panel.add(Box.createVerticalStrut(10));
        panel.add(viewTeacherResourcesButton);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            previousFrame.setVisible(true);
            dispose();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(backButton);

        loadCourses(level);

        add(panel);
        setVisible(true);
    }

    private void loadCourses(int level) {
        if (resourceManager == null) {
            resourceManager = new ResourceManager();
        }

        List<Course> courses = resourceManager.getCourses(level);
        for (Course course : courses) {
            courseBox.addItem(course.getName());
        }
    }

    private void handleSubmit() {
        String selectedCourse = (String) courseBox.getSelectedItem();
        String resourceType = (String) resourceBox.getSelectedItem();

        if (selectedCourse == null || resourceType == null) {
            JOptionPane.showMessageDialog(this, "Please select a course and resource type.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (resourceType.equals("Books")) {
            List<String> imageUrls = CourseData.getImageUrls(selectedCourse, level);

            if (imageUrls != null) {
                List<String> imageTitles = CourseData.getImageTitles(selectedCourse, level);
                List<String> imageIds = CourseData.getImageIDs(selectedCourse, level);

                Book selectedBook = new Book(imageUrls, imageTitles, imageIds);
                setVisible(false);
                new BookInfoPage(selectedBook, this);
            } else {
                JOptionPane.showMessageDialog(this, "No images found for this course.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            String videoTitle = selectedCourse + " Video";
            String videoImagePath = "";
            String videoUrl = "";

            if (level == 1) {
                List<String> imageUrls = CourseData.getVideoImages(selectedCourse, level);

                if (imageUrls != null) {
                    List<String> imageTitles = CourseData.getVideoImageTitles(selectedCourse, level);

                    Video selectedVideo = new Video(imageUrls, imageTitles);
                    setVisible(false);
                    new VideoInfoPage(selectedVideo, this);
                } else {
                    JOptionPane.showMessageDialog(this, "No images found for this course.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleViewTeacherResources() {
        String selectedCourse = (String) courseBox.getSelectedItem();

        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this, "Please select a course.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Course selectedCourseObj = null;
        for (Course course : resourceManager.getCourses(level)) {
            if (course.getName().equals(selectedCourse)) {
                selectedCourseObj = course;
                break;
            }
        }

        if (selectedCourseObj == null) {
            JOptionPane.showMessageDialog(this, "No resources found for this course.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CourseResource resources = selectedCourseObj.getResources();

        JFrame resourcesFrame = new JFrame("Teacher Resources for " + selectedCourse);
        resourcesFrame.setSize(400, 300);
        resourcesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Resources for " + selectedCourse);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        List<String> resourceTypes = resources.getResourceTypes();
        if (resourceTypes.isEmpty()) {
            JLabel noResourcesLabel = new JLabel("No resources available.");
            noResourcesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(noResourcesLabel);
        } else {
            for (String resourceType : resourceTypes) {
                JLabel resourceLabel = new JLabel(resourceType + ":");
                resourceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(resourceLabel);

                List<String> resourceLinks = resources.getResourceLinks(resourceType);
                if (resourceLinks.isEmpty()) {
                    JLabel noLinksLabel = new JLabel("No links available.");
                    noLinksLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panel.add(noLinksLabel);
                } else {
                    for (String link : resourceLinks) {
                        JButton linkButton = new JButton(link);
                        linkButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                        linkButton.addActionListener(e -> {
                            try {
                                Desktop.getDesktop().browse(new URI(link));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        panel.add(linkButton);
                    }
                }
            }
        }

        resourcesFrame.add(panel);
        resourcesFrame.setVisible(true);
    }
}

//-------------------------------------------------------------------------------------------

class TeacherGUI extends JFrame {
    private JTextField emailField;
    private JComboBox<Integer> levelBox;
    private JButton nextButton, backButton;

    public TeacherGUI() {
        this.setTitle("Enter Details");
        this.setSize(350, 220);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(239,235,223));

        JLabel idLabel = new JLabel("Enter your University Email:");
        idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(idLabel);

        emailField = new JTextField(15);
        emailField.setMaximumSize(new Dimension(200, 25));
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(emailField);
        panel.add(Box.createVerticalStrut(10));

        JLabel levelLabel = new JLabel("Choose your level:");
        levelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(levelLabel);

        levelBox = new JComboBox<>(new Integer[]{1, 2});
        levelBox.setMaximumSize(new Dimension(200, 25));
        levelBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(levelBox);
        panel.add(Box.createVerticalStrut(10));

        nextButton = new JButton("Next");
        nextButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextButton.addActionListener(e -> openTeacherGUI());
        panel.add(nextButton);

        backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            this.dispose();
            new LibraryWelcomeGUI();
        });
        panel.add(backButton);

        this.add(panel);
        this.setVisible(true);
    }

    private void openTeacherGUI() {
        String email = emailField.getText().trim();
        if (!email.matches("^[a-zA-Z0-9._%+-]+@iau.edu.sa$")) {
            JOptionPane.showMessageDialog(this, "Invalid University ID! Must end with '@iau.edu.sa'.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!checkEMILInDatabase(email)) {
            JOptionPane.showMessageDialog(this, "This email is not registered in the system!", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int level = (int) levelBox.getSelectedItem();
        this.dispose();
        new TeacherGUI2(email, level);
    }

    private boolean checkEMILInDatabase(String email) {
        String query = "SELECT * FROM TEACHER WHERE T_EMAIL = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error while checking email.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}

class TeacherGUI2 extends JFrame {
    private String universityID;
    private int level;
    private JComboBox<String> courseBox;
    private JComboBox<String> resourceBox;
    private JButton addButton, removeButton, viewButton, backButton;
    private ResourceManager resourceManager;

    public TeacherGUI2(String universityID, int level) {
        this.universityID = universityID;
        this.level = level;

        Color backgroundColor = new Color(239, 235, 223);

        this.setTitle("Teacher Portal");
        this.setSize(650, 350);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(new JLabel("Email: " + universityID), gbc);

        gbc.gridx = 1;
        mainPanel.add(new JLabel("Level: " + level), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Choose a course you want to modify:"), gbc);

        courseBox = new JComboBox<>();
        courseBox.setBackground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(courseBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("Choose resource type:"), gbc);

        resourceBox = new JComboBox<>(new String[]{"Video", "Book"});
        resourceBox.setBackground(Color.WHITE);
        gbc.gridx = 1;
        mainPanel.add(resourceBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(backgroundColor);

        addButton = new JButton("Add Resource");
        removeButton = new JButton("Remove Resource");
        viewButton = new JButton("View Resources");
        backButton = new JButton("Back");

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        addButton.addActionListener(e -> handleAddResource());
        removeButton.addActionListener(e -> handleRemoveResource());
        viewButton.addActionListener(e -> handleViewResources());

        backButton.addActionListener(e -> {
            this.dispose();
            new TeacherGUI();
        });

        loadCourses();

        this.setContentPane(mainPanel);
        this.setVisible(true);
    }

    private void loadCourses() {
        if (resourceManager == null) {
            resourceManager = new ResourceManager();
        }

        List<Course> courses = resourceManager.getCourses(level);
        courseBox.removeAllItems();
        for (Course course : courses) {
            courseBox.addItem(course.getName());
        }
    }

    private void handleAddResource() {
        String resourceLink = JOptionPane.showInputDialog(this, "Enter the resource link to add:");
        if (resourceLink != null && !resourceLink.isEmpty()) {
            String resourceType = (String) resourceBox.getSelectedItem();
            Course selectedCourse = getSelectedCourse();
            if (selectedCourse != null) {
                selectedCourse.getResources().addResource(resourceType, resourceLink);
                addResourceToDatabase(resourceLink);
                JOptionPane.showMessageDialog(this, "Resource added successfully!");
            }
        }
    }

    private void handleRemoveResource() {
        Course selectedCourse = getSelectedCourse();
        if (selectedCourse != null) {
            String resourceType = (String) resourceBox.getSelectedItem();
            List<String> resourceList = selectedCourse.getResources().getResources(resourceType);
            if (resourceList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No resources to remove.");
                return;
            }

            String resourceToRemove = (String) JOptionPane.showInputDialog(
                    this, "Choose a resource to remove:",
                    "Remove Resource", JOptionPane.PLAIN_MESSAGE, null,
                    resourceList.toArray(), resourceList.get(0));

            if (resourceToRemove != null) {
                removeResourceFromDatabase(resourceToRemove);
                selectedCourse.getResources().removeResource(resourceType, resourceList.indexOf(resourceToRemove));
                JOptionPane.showMessageDialog(this, "Resource removed successfully!");
            }
        }
    }

    private void handleViewResources() {
        Course selectedCourse = getSelectedCourse();
        if (selectedCourse != null) {
            String resourceType = (String) resourceBox.getSelectedItem();
            List<String> resourceList = selectedCourse.getResources().getResources(resourceType);
            StringBuilder message = new StringBuilder("Resources for " + resourceType + ":\n");
            for (String resource : resourceList) {
                message.append("- ").append(resource).append("\n");
            }
            JOptionPane.showMessageDialog(this, message.toString());
        }
    }

    private Course getSelectedCourse() {
        int selectedIndex = courseBox.getSelectedIndex();
        if (selectedIndex >= 0) {
            List<Course> availableCourses = resourceManager.getCourses(level);
            return availableCourses.get(selectedIndex);
        }
        return null;
    }

    public void addResourceToDatabase(String resourceLink) {
        try (Connection conn = DBConnection.connect()) {
            String query = "INSERT INTO COURSE_RESOURCES (RESOURCE_LINK) VALUES ('" + resourceLink + "')";
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(query);
                System.out.println("Resource added successfully!");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to add resource.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database connection failed.");
        }
    }

    private void removeResourceFromDatabase(String resourceLink) {
        String query = "DELETE FROM COURSE_RESOURCES WHERE RESOURCE_LINK = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, resourceLink);
            pstmt.executeUpdate();
            System.out.println("Resource deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to delete resource.");
        }
    }
}


//----------------------------------------------------------------------------------------------

class ResourceManager {
    private Map<Integer, List<Course>> courses = new HashMap<>();

    public ResourceManager() {
        initializeCourses();
    }

    private void initializeCourses() {
        courses.put(1, Arrays.asList(new Course("AI"), new Course("Cyber Security"), new Course("Calculus")));
        courses.put(2, Arrays.asList(new Course("OOP"), new Course("Statistics"), new Course("Network")));
    }

    public List<Course> getCourses(int level) {
        return courses.get(level);
    }

public Course getCourseByName(int level, String name) {
    List<Course> courseList = courses.get(level);
    if (courseList != null) {
        for (Course course : courseList) {
            if (course.getName().equalsIgnoreCase(name)) {
                return course;
            }
        }
    }
    return null;
}}

//----------------------------------------------------------------------------------

class Course {
    private String name;
    private CourseResource resources = new CourseResource();

    public Course(String name) {
        this.name = name;
        initializeResources();
    }

    private void initializeResources() { 
        switch (name) {
            case "AI":
                resources.addResource("Book", "https://drive.google.com/file/d/11bPP-ftKjhPCbcObAbGP1g8KqKRvnoYP/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1ChuJSMypR7sSX9zOYcXsGMFDRbaGTrPn/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/NdJtQDlUegs?si=s6Hi14ydH2irrH9K");
                resources.addResource("Video", "https://youtu.be/1iba4Sb3qtk?si=Ksz7YAtRSgoKNxN9");
                break;
            case "Cyber Security":
                resources.addResource("Book", "https://drive.google.com/file/d/1jTXxoFKfYiJPn1CDC0jK9e2xSQnqZ7Bn/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1TMRIsfsYZ9vN8XmA-20TQjOeKuR-ypYU/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/Fh9LE4-YlNI?si=raqMCr6H0Qcf29zd");
                resources.addResource("Video", "https://youtu.be/oeQn42dNi24?si=a0zc5oMcgtLPKp4I");
                break;
            case "Calculus":
                resources.addResource("Book", "https://drive.google.com/file/d/1cu9Go_gn5zmRJyIZqEULdU8gNr2E1Txx/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1w2TDXifs2yMakFjTcWhXTSe_fSgA9Vlv/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/IHLBbSTFxRA?si=pwxDlIusIHxs1nsb");
                resources.addResource("Video", "https://youtu.be/NpIrlfJ9550?si=rbNrhxI5glJebvb8");
                break;
            case "OOP":
                resources.addResource("Book", "https://drive.google.com/file/d/1u88JAD7O0SAoAcuoQNEps5LBB1Zf13pH/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1kSMZrVMQ8kWGFWRbk6YF5Lg17WGIg7G7/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/M3Na5luSx50?si=mweExiEqXVuv72Dm");
                resources.addResource("Video", "https://youtu.be/QEcGBVPjGio?si=jt7kusxbm04-NGm_");
                break;
            case "Statistics":
                resources.addResource("Book", "https://drive.google.com/file/d/1_rXqe4tWRAEPGGwbLtih9U7OY7iWxR36/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1d-vq3ALSe7ehUA-BNkY4PhO3dYM3yZ4o/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/kHP_CYmVuEE?si=FFhWnvtRqGgNJcem");
                resources.addResource("Video", "https://youtu.be/7YCG1vcn0EE?si=OOx5tu5bDQYdYU5H");
                break;
            case "Network":
                resources.addResource("Book", "https://drive.google.com/file/d/1bKKD3iuZmHttkcsbeTa05PE2MpEdib2M/view?usp=drivesdk");
                resources.addResource("Book", "https://drive.google.com/file/d/1kx_YcxRPFT9D4c3fWf8g7W1R7uxq0Rhe/view?usp=drivesdk");
                resources.addResource("Video", "https://youtu.be/9aOWD34RIVo?si=BwgZVubgvBXSagZo");
                resources.addResource("Video", "https://youtu.be/gUSR72Sbz_I?si=88rQKttvH84fq1z6");
                break;
        }
    }

    public String getName() {
        return name;
    }

    public CourseResource getResources() {
        return resources;
    }
}

//--------------------------------------------------------------------------------------------

class CourseResource {
    private List<String> videos = new ArrayList<>();
    private List<String> books = new ArrayList<>();

    public void addResource(String type, String link) {
        if (type.equals("Video")) {
            videos.add(link);
        } else if (type.equals("Book")) {
            books.add(link);
        }
    }

    public void removeResource(String type, int index) {
        if (type.equals("Video") && index >= 0 && index < videos.size()) {
            videos.remove(index);
        } else if (type.equals("Book") && index >= 0 && index < books.size()) {
            books.remove(index);
        }
    }

    public List<String> getResources(String type) {
        return type.equals("Video") ? videos : books;
    }
    
    public List<String> getResourceTypes() {
        List<String> types = new ArrayList<>();
        if (!videos.isEmpty()) types.add("Video");
        if (!books.isEmpty()) types.add("Book");
        return types;
    }

    public List<String> getResourceLinks(String type) {
        if (type.equals("Video")) {
            return videos;
        } else if (type.equals("Book")) {
            return books;
        }
        return new ArrayList<>();
    }
}

//---------------------------------------------------------------------------------------------------------------------

class CourseData {

    // AI
    public static final List<String> AI_IMAGES = Arrays.asList("AI1.PNG", "AI2.PNG", "AI3.JPG", "AI4.jpeg", "AI5.JPG", "AI6.JPG");
    public static final List<String> AI_TITLES = Arrays.asList("AI Basics", "Machine Learning", "Deep Learning", "Neural Networks", "AI Ethics", "AI Tools");
    public static final List<String> AI_IDS = Arrays.asList("AI1", "AI2", "AI3", "AI4", "AI5", "AI6");

    // Cyber Security
    public static final List<String> CYBER_SECURITY_IMAGES = Arrays.asList("CY1.PNG", "CY2.PNG", "CY3.JPG", "CY4.JPG", "CY5.JPG", "CY6.JPG");
    public static final List<String> CYBER_TITLES = Arrays.asList("Intro to Security", "Encryption", "Firewalls", "Threat Analysis", "Ethical Hacking", "Network Security");
    public static final List<String> CYBER_IDS = Arrays.asList("CY1", "CY2", "CY3", "CY4", "CY5", "CY6");

    // Calculus
    public static final List<String> CALCULUS_IMAGES = Arrays.asList("cal1.png", "cal2.png", "cal3.JPG", "cal4.JPG", "cal5.JPG", "cal6.JPG");
    public static final List<String> CALCULUS_TITLES = Arrays.asList("Limits", "Derivatives", "Integrals", "Series", "Applications", "Multivariable");
    public static final List<String> CALCULUS_IDS = Arrays.asList("CAL1", "CAL2", "CAL3", "CAL4", "CAL5", "CAL6");

    // OOP
    public static final List<String> OOP_IMAGES = Arrays.asList("JV1.PNG", "JV2.PNG", "JV3.JPG", "JV4.JPG", "JV5.JPG", "JV6.JPG");
    public static final List<String> OOP_TITLES = Arrays.asList("Classes", "Objects", "Inheritance", "Polymorphism", "Abstraction", "Encapsulation");
    public static final List<String> OOP_IDS = Arrays.asList("OOP1", "OOP2", "OOP3", "OOP4", "OOP5", "OOP6");

    // Statistics
    public static final List<String> STATISTICS_IMAGES = Arrays.asList("ST1.PNG", "ST2.PNG", "ST3.JPG", "ST4.JPG", "ST5.JPG", "ST6.JPG");
    public static final List<String> STATISTICS_TITLES = Arrays.asList("Data Types", "Probability", "Distributions", "Hypothesis Testing", "Regression", "ANOVA");
    public static final List<String> STATISTICS_IDS = Arrays.asList("ST1", "ST2", "ST3", "ST4", "ST5", "ST6");

    // Network
    public static final List<String> NETWORK_IMAGES = Arrays.asList("NT1.PNG", "NT2.PNG", "NT3.JPG", "NT4.JPG", "NT5.JPG", "NT6.JPG");
    public static final List<String> NETWORK_TITLES = Arrays.asList("Intro to Networking", "OSI Model", "IP Addressing", "Routing", "Switching", "Protocols");
    public static final List<String> NETWORK_IDS = Arrays.asList("NT1", "NT2", "NT3", "NT4", "NT5", "NT6");

    public static List<String> getImageUrls(String course, int level) {
        if (level == 1) {
            switch (course) {
                case "AI":
                    return AI_IMAGES;
                case "Cyber Security":
                    return CYBER_SECURITY_IMAGES;
                case "Calculus":
                    return CALCULUS_IMAGES;
                default:
                    return null;
            }
        } else if (level == 2) {
            switch (course) {
                case "OOP":
                    return OOP_IMAGES;
                case "Statistics":
                    return STATISTICS_IMAGES;
                case "Network":
                    return NETWORK_IMAGES;
                default:
                    return null;
            }
        }
        return null;
    }

    public static List<String> getImageTitles(String course, int level) {
        if (level == 1) {
            switch (course) {
                case "AI":
                    return AI_TITLES;
                case "Cyber Security":
                    return CYBER_TITLES;
                case "Calculus":
                    return CALCULUS_TITLES;
                default:
                    return null;
            }
        } else if (level == 2) {
            switch (course) {
                case "OOP":
                    return OOP_TITLES;
                case "Statistics":
                    return STATISTICS_TITLES;
                case "Network":
                    return NETWORK_TITLES;
                default:
                    return null;
            }
        }
        return null;
    }

    public static List<String> getImageIDs(String course, int level) {
        if (level == 1) {
            switch (course) {
                case "AI":
                    return AI_IDS;
                case "Cyber Security":
                    return CYBER_IDS;
                case "Calculus":
                    return CALCULUS_IDS;
                default:
                    return null;
            }
        } else if (level == 2) {
            switch (course) {
                case "OOP":
                    return OOP_IDS;
                case "Statistics":
                    return STATISTICS_IDS;
                case "Network":
                    return NETWORK_IDS;
                default:
                    return null;
            }
        }
        return null;
    }
    
    // AI
public static final List<String> AI_VideoIMAGES = Arrays.asList("AI1.PNG", "AI2.PNG", "AI3.JPG");
public static final List<String> AI_VideoTITLES = Arrays.asList("AI Basics", "Machine Learning", "Deep Learning");

// Cyber Security
public static final List<String> CYBER_SECURITY_VideoIMAGES = Arrays.asList("CY1.PNG", "CY2.PNG", "CY3.JPG");
public static final List<String> CYBER_SECURITY_VideoTITLES = Arrays.asList("Intro to Security", "Encryption", "Firewalls");

// Calculus
public static final List<String> CALCULUS_VideoIMAGES = Arrays.asList("cal1.png", "cal2.png", "cal3.JPG");
public static final List<String> CALCULUS_VideoTITLES = Arrays.asList("Limits", "Derivatives", "Integrals");

// OOP
public static final List<String> OOP_VideoIMAGES = Arrays.asList("JV1.PNG", "JV2.PNG", "JV3.JPG");
public static final List<String> OOP_VideoTITLES = Arrays.asList("Classes", "Objects", "Inheritance");

// Statistics
public static final List<String> STATISTICS_VideoIMAGES = Arrays.asList("ST1.PNG", "ST2.PNG", "ST3.JPG");
public static final List<String> STATISTICS_VideoTITLES = Arrays.asList("Data Types", "Probability", "Distributions");

// Network
public static final List<String> NETWORK_VideoIMAGES = Arrays.asList("NT1.PNG", "NT2.PNG", "NT3.JPG");
public static final List<String> NETWORK_VideoTITLES = Arrays.asList("Intro to Networking", "OSI Model", "IP Addressing");



    public static List<String> getVideoImageTitles(String course, int level) {
    if (level == 1) {
        switch (course) {
            case "AI":
                return AI_VideoTITLES;
            case "Cyber Security":
                return CYBER_SECURITY_VideoTITLES;
            case "Calculus":
                return CALCULUS_VideoTITLES;
            default:
                return null;
        }
    } else if (level == 2) {
        switch (course) {
            case "OOP":
                return OOP_VideoTITLES;
            case "Statistics":
                return STATISTICS_VideoTITLES;
            case "Network":
                return NETWORK_VideoTITLES;
            default:
                return null;
        }
    }
    return null;
}
    public static List<String> getVideoImages(String course, int level) {
    if (level == 1) {
        switch (course) {
            case "AI":
                return AI_VideoIMAGES;
            case "Cyber Security":
                return CYBER_SECURITY_VideoIMAGES;
            case "Calculus":
                return CALCULUS_VideoIMAGES;
            default:
                return null;
        }
    } else if (level == 2) {
        switch (course) {
            case "OOP":
                return OOP_VideoIMAGES;
            case "Statistics":
                return STATISTICS_VideoIMAGES;
            case "Network":
                return NETWORK_VideoIMAGES;
            default:
                return null;
        }
    }
    return null;
}


}

//---------------------------------------------------------------------------------------------------------------------


class Book {
    private List<String> imageUrls;
    private List<String> imageTitles;
    private List<String> imageIds;

    
    public Book(List<String> imageUrls, List<String> imageTitles, List<String> imageIds) {
        
        this.imageUrls = imageUrls;
        this.imageTitles = imageTitles;
        this.imageIds = imageIds;
    }

    
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public List<String> getImageTitles() {
        return imageTitles;
    }

    public void setImageTitles(List<String> imageTitles) {
        this.imageTitles = imageTitles;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }
}

class BookInfoPage extends JFrame {
    private JFrame previousFrame;

    public BookInfoPage(Book book, JFrame previousFrame) {
        this.previousFrame = previousFrame;

        setTitle("Book Information");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(239, 235, 223));

        JPanel imagePanel = new JPanel(new GridLayout(2, 3, 10, 10));
        imagePanel.setBackground(new Color(239, 235, 223)); 
        imagePanel.setOpaque(true);
        
        List<String> imageUrls = book.getImageUrls();
        List<String> imageTitles = book.getImageTitles();
        List<String> imageIds = book.getImageIds();
        int imageCount = Math.min(imageUrls.size(), 6);

        for (int i = 0; i < imageCount; i++) {
            ImageIcon bookImage = new ImageIcon(imageUrls.get(i));
            Image image = bookImage.getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH);
            bookImage = new ImageIcon(image);

            JLabel imageLabel = new JLabel(bookImage);

            JPanel imageWithDetails = new JPanel();
            imageWithDetails.setLayout(new BoxLayout(imageWithDetails, BoxLayout.Y_AXIS));
            JLabel imageTitle = new JLabel("Title: " + imageTitles.get(i));
            JLabel imageId = new JLabel("ID: " + imageIds.get(i));

            imageWithDetails.add(imageLabel);
            imageWithDetails.add(imageTitle);
            imageWithDetails.add(imageId);
            
            imageWithDetails.setBackground(new Color(239, 235, 223));
            imageWithDetails.setOpaque(true);

            imagePanel.add(imageWithDetails);
        }

        panel.add(imagePanel);

        JLabel bookIdLabel = new JLabel("Enter the ID of the book:");
        bookIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(bookIdLabel);

        JTextField bookIdField = new JTextField(10);
        bookIdField.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookIdField.setMaximumSize(new Dimension(50, 50));
        panel.add(bookIdField);

        JButton borrowButton = new JButton("Borrow Book");
        borrowButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        borrowButton.addActionListener(e -> handleBorrow(book, bookIdField.getText()));
        panel.add(borrowButton);

        add(panel);
        setVisible(true);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            previousFrame.setVisible(true);
            dispose();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(backButton);
    }

    private void handleBorrow(Book book, String bookId) {
        if (bookId == null || bookId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the ID of the book.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Connection conn = DBConnection.connect();

            String query = "SELECT BORROW_DATE, RETURN_DATE, LINK FROM BORROWBOOK JOIN BOOK ON BORROWBOOK.BOOK_ID = BOOK.BOOKID WHERE BORROWBOOK.BOOK_ID = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, bookId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Date borrowDate = rs.getDate("BORROW_DATE");
                Date returnDate = rs.getDate("RETURN_DATE");
                String resourceLink = rs.getString("LINK");

                JOptionPane.showMessageDialog(this,
                        "Book borrowed successfully!\n" +
                        "Title: " + book.getImageTitles() + "\n" +
                        "Borrow Date: " + borrowDate + "\n" +
                        "Return Date: " + returnDate + "\n" +
                        "Resource Link: " + resourceLink,
                        "Borrow Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No borrow record found for this book.", "Not Found", JOptionPane.ERROR_MESSAGE);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}



class BorrowBook{
    private int bookID;
    private String studentID;
    private Date borrowDate;
    private Date returnDate;
    private boolean isReturned;

    public BorrowBook(int bookID, String studentID, Date borrowDate, Date returnDate) {
        this.bookID = bookID;
        this.studentID = studentID;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.isReturned = false; 
    }

    public int getBookID() {
        return bookID;
    }

    public String getStudentID() {
        return studentID;
    }

    public Date getBorrowDate() {
        return borrowDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public void returnBook() {
        this.isReturned = true;
        System.out.println("Book ID: " + bookID + " has been returned by Student ID: " + studentID);
    }

    @Override
    public String toString() {
        return "Book ID: " + bookID + ", Borrowed by: " + studentID + ", Borrow Date: " + borrowDate +
                ", Return Date: " + returnDate + ", Status: " + (isReturned ? "Returned" : "Not Returned");
    }
}
//--------------------------------------------------------------------------------------------

class Video {
    private List<String> videoUrls;
    private List<String> videoTitles;

    public Video(List<String> videoUrls, List<String> videoTitles) {
        this.videoUrls = videoUrls;
        this.videoTitles = videoTitles;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }

    public List<String> getVideoTitles() {
        return videoTitles;
    }

    public void setVideoTitles(List<String> videoTitles) {
        this.videoTitles = videoTitles;
    }
}


class VideoInfoPage extends JFrame {
    private JFrame previousFrame;

    public VideoInfoPage(Video video, JFrame previousFrame) {
        this.previousFrame = previousFrame;

        setTitle("Channel Information");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(239, 235, 223));

        List<String> videoUrls = video.getVideoUrls();
        List<String> videoTitles = video.getVideoTitles();
        int videoCount = Math.min(videoUrls.size(), 6);

        JPanel videoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        videoPanel.setBackground(new Color(239, 235, 223));
        videoPanel.setOpaque(true);

        for (int i = 0; i < videoCount; i++) {
            ImageIcon videoImage = new ImageIcon(videoUrls.get(i));
            Image image = videoImage.getImage().getScaledInstance(100, 150, Image.SCALE_SMOOTH);
            videoImage = new ImageIcon(image);

            JLabel imageLabel = new JLabel(videoImage);

            JPanel videoWithDetails = new JPanel();
            videoWithDetails.setLayout(new BoxLayout(videoWithDetails, BoxLayout.Y_AXIS));
            videoWithDetails.setBackground(new Color(239, 235, 223));
            videoWithDetails.setOpaque(true);

            JLabel videoTitle = new JLabel("Title: " + videoTitles.get(i));
            videoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            videoWithDetails.add(imageLabel);
            videoWithDetails.add(Box.createVerticalStrut(5));
            videoWithDetails.add(videoTitle);

            videoPanel.add(videoWithDetails);
        }

        panel.add(videoPanel);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> {
            previousFrame.setVisible(true);
            dispose();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(backButton);

        add(panel);
        setVisible(true);
    }
}



//--------------------------------------------------------------------------------------------





