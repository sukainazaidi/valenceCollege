//Sukaina Zaidi

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Scanner;


public class ProjectDriver {

    private static String mainMenu(){
        String option;
        Scanner scanner = new Scanner(System.in);


        System.out.println("Main Menu:");
        System.out.println("\t1 - Student Management");
        System.out.println("\t2 - Course Management");
        System.out.println("\t0 - Exit");
        option = scanner.nextLine();

        return option;
    }

    private static String studentMenu(){
        String studentOption;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Student Management Menu:");
        System.out.println("\t A - Search add a student");
        System.out.println("\t B - Delete a Student");
        System.out.println("\t C - Print Fee Invoice");
        System.out.println("\t D - Print List of Students");
        System.out.println("\t X - Back to Main Menu");
        studentOption = scanner.nextLine().toUpperCase();

        return studentOption;
    }

    private static String courseMenu(){
        String courseOption;
        Scanner scanner = new Scanner(System.in);


        System.out.println("Course Management Menu:");
        System.out.println("\t A - Search for a class or lab using the class/lab number");
        System.out.println("\t B - Delete a class");
        System.out.println("\t C - Add a lab to a class");
        System.out.println("\t D - Print lectures and labs");
        System.out.println("\t X - Back to main menu");
        courseOption = scanner.nextLine().toUpperCase();

        return courseOption;
    }

    public static void writeArrayListToFile(ArrayList<Lecture> listOfLectures) {
        try {
            PrintWriter pw = new PrintWriter("lec.txt");

            // Iterate through the ArrayList and write each element to the file
            for (Lecture lec : listOfLectures) {
                pw.print(lec);

                // If the lecture has labs
                if (lec.getLabs() != null) {
                    for (Lab lab : lec.getLabs()) {
                        pw.print(lab);
                    }
                }
            }

            pw.close();

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws FileNotFoundException {
        String option;
        String studentOption;
        String courseOption;
        Scanner scanner = new Scanner(System.in);

        ClassManagement classManagement;
        ArrayList<Lecture> listOfLectures = ClassesFromFile.loadLecturesFromFile();
        classManagement = new ClassManagement(listOfLectures);
        StudentManagement studentManagement = new StudentManagement();


        do {
            option = mainMenu();

            switch (option) {
                case "1":
                    do {
                        studentOption = studentMenu();
                        switch (studentOption) {
                            case "A":
                                System.out.println("Add a New Student:");


                                System.out.print("Enter Student's ID: ");
                                String id = scanner.nextLine();

                                try {
                                    if (!id.matches("[a-zA-Z]{2}\\d{4}")) {
                                        throw new IdException("ID must be of the form LetterLetterDigitDigitDigitDigit (LLDDDD)\n");
                                    }
                                } catch (IdException e) {
                                    System.out.println("Error: " + e.getMessage());
                                    break;
                                }

                                if (studentManagement.isDuplicateId(id)) {
                                    System.out.println("\nID is taken, please enter a new ID\n");
                                    break;
                                }

                                System.out.print("Student Type (Undergrad, Masters, PhD): ");
                                String studentType = scanner.nextLine().toUpperCase();

                                if (studentType.equals("UNDERGRAD")) {
                                    System.out.println("Enter remaining info: name|resident?(yes/no)|gpa|crn(s)");
                                    String remainingInfo = scanner.nextLine();
                                    String[] origInfo = remainingInfo.split("\\|");
                                    String name = origInfo[0];
                                    boolean resident = origInfo[1].equalsIgnoreCase("yes");
                                    double gpa = Double.parseDouble(origInfo[2]);
                                    String[] crns = origInfo[3].split(",");

                                    // Ensure undergrad register only for undergrad courses
                                    boolean allUndergrad = Arrays.stream(crns).allMatch(crn -> {
                                        Lecture lecture = classManagement.getLectureByCrn(crn);
                                        return lecture != null && lecture.getLectureType() == LectureType.UNDERGRAD;
                                    });

                                    if (!allUndergrad) {
                                        System.out.println("\nError: Undergrad students can only register for Undergraduate courses.\n");
                                        break;
                                    }

                                    //add class to arrayofcourses and add crn to arraylist of crns w student
                                    ArrayList<Lecture> enrolledCourses = new ArrayList<>();
                                    for (String crn : crns) {
                                        Lecture lecture = classManagement.getLectureByCrn(crn);
                                        enrolledCourses.add(lecture);
                                        classManagement.addCourseWithStudents(crn);
                                    }

                                    studentManagement.addStudent(name, id, gpa, resident, enrolledCourses);
                                    System.out.println("\nStudent added successfully.\n");
                                } else if (studentType.equals("MASTERS")) {
                                    System.out.println("Enter remaining info: name|crn(s)");
                                    String remainingInfo = scanner.nextLine();
                                    String[] origInfo = remainingInfo.split("\\|");
                                    String name = origInfo[0];
                                    String[] crns = origInfo[1].split(",");

                                    // ensure masters student is only enrolling for graduate courses
                                    boolean allGrad = Arrays.stream(crns).allMatch(crn -> {
                                        Lecture lecture = classManagement.getLectureByCrn(crn);
                                        return lecture != null && lecture.getLectureType() == LectureType.GRAD;
                                    });

                                    if (!allGrad) {
                                        System.out.println("\nError: Masters students can only register for Graduate courses.\n");
                                        break;
                                    }

                                    ArrayList<Lecture> enrolledCourses = new ArrayList<>();
                                    for (String crn : crns) {
                                        Lecture lecture = classManagement.getLectureByCrn(crn);
                                        enrolledCourses.add(lecture);
                                        classManagement.addCourseWithStudents(crn);
                                    }

                                    studentManagement.addStudent(name, id, enrolledCourses);
                                    System.out.println("\nStudent added successfully.\n");
                                } else if (studentType.equals("PHD")) {
                                    System.out.println("Enter remaining info: name|advisor|research|labs supervising");
                                    String remainingInfo = scanner.nextLine();
                                    String[] origInfo = remainingInfo.split("\\|");
                                    String name = origInfo[0];
                                    String advisor = origInfo[1];
                                    String researchSubject = origInfo[2];
                                    String[] crns = origInfo[3].split(",");

                                    // ensure phd is only supervising labs
                                    boolean allLabs = Arrays.stream(crns).allMatch(crn -> {
                                        for (Lecture lecture : listOfLectures) {
                                            if (lecture.getLabs() != null) {
                                                for (Lab lab : lecture.getLabs()) {
                                                    if (lab.getCrn().equals(crn)) {
                                                        return true; // Found the CRN in labs, valid
                                                    }
                                                }
                                            }
                                        }
                                        return false; // CRN not found in any labs
                                    });

                                    if (!allLabs) {
                                        System.out.println("\nError: PhD students can only supervise labs.\n");
                                        break;
                                    }

                                    ArrayList<Lecture> enrolledCourses = new ArrayList<>();
                                    for (String crn : crns) {
                                        Lecture lecture = classManagement.getLectureByCrn(crn);
                                        enrolledCourses.add(lecture);
                                        classManagement.addCourseWithStudents(crn);
                                    }

                                    studentManagement.addStudent(name, id, advisor, researchSubject, enrolledCourses);
                                    System.out.println("\nStudent added successfully.\n");
                                }
                                break;

                            case "B":
                                System.out.print("Enter student id: ");
                                String deleteId = scanner.nextLine();
                                System.out.println("\n Deleting student ...");
                                studentManagement.deleteStudentById(deleteId);
                                break;

                            case "C":
                                System.out.print("Enter student id: ");
                                String invoiceId = scanner.nextLine();
                                studentManagement.printFeeInvoice(invoiceId);
                                break;

                            case "D":
                                studentManagement.printListOfStudents();
                                break;
                        }

                    } while (!studentOption.equals("X")) ;
                        break;


                        case "2":
                            do {
                                courseOption = courseMenu();
                                switch (courseOption) {
                                    case "A":
                                        System.out.println("Searching for a class or lab");
                                        System.out.print("Enter crn: ");
                                        String searchCrn = scanner.nextLine();
                                        classManagement.searchClassByCrn(searchCrn);
                                        break;
                                    case "B":
                                        System.out.println("Deleting a class");
                                        System.out.print("Enter CRN: ");
                                        String deleteCrn = scanner.nextLine();

                                        Lecture lectureToDelete = classManagement.getLectureByCrn(deleteCrn);

                                        if (lectureToDelete != null) {
                                            if (lectureToDelete.isHasLabs()) {
                                                System.out.println("\nThis class has labs associated with it and cannot be deleted at this time.\n");
                                            } else {
                                                if (classManagement.hasStudents(deleteCrn)) {
                                                    System.out.println("\nThis class has students and cannot be deleted at this time.\n");
                                                } else {
                                                    classManagement.deleteClassByCrn(deleteCrn);
                                                    writeArrayListToFile(listOfLectures);
                                                    System.out.println("Class deleted successfully.");
                                                }
                                            }
                                        } else {
                                            System.out.println("Class not found with CRN: " + deleteCrn);
                                        }


                                        break;
                                    case "C":
                                        System.out.println("Adding a lab to a class");
                                        System.out.print("Enter CRN of lecture to add lab to: ");
                                        String lectureCrn = scanner.nextLine();

                                        // Find the lecture by CRN
                                        Lecture selectedLecture = null;
                                        for (Lecture lecture : listOfLectures) {
                                            if (lecture.getCrn().equals(lectureCrn)) {
                                                selectedLecture = lecture;
                                                break;
                                            }
                                        }

                                        if (selectedLecture != null) {
                                            if (selectedLecture.isHasLabs()) {
                                                System.out.println(lectureCrn + " is valid. Enter lab CRN, lab room number");
                                                String labInfo = scanner.nextLine();
                                                String[] labDetails = labInfo.split(",");
                                                String labCrn = labDetails[0];
                                                String labClassroom = labDetails[1];

                                                // Create a new Lab object
                                                Lab newLab = new Lab(labCrn, labClassroom);

                                                // Add the new lab to the lecture's list of labs
                                                selectedLecture.getLabs().add(newLab);
                                                writeArrayListToFile(listOfLectures);

                                                System.out.println("Lab added successfully to the lecture.");
                                            } else {
                                                System.out.println("This lecture does not have labs. You can only add labs to lectures that already have labs.");
                                            }
                                        } else {
                                            System.out.println("Class not found with CRN: " + lectureCrn);
                                        }
                                        break;

                                    case "D":
                                        // Check if the list is empty
                                        if (listOfLectures.isEmpty()) {
                                            System.out.println("No lectures loaded from the file.");
                                        } else {
                                            // Print each lecture
                                            for (Lecture lecture : listOfLectures) {
                                                System.out.println(lecture);
                                            }
                                        }
                                        break;


                                    case "X":
                                        break; // Exit the loop to return to main menu
                                    default:
                                        System.out.println("Invalid option");
                                        break;
                                }
                            } while (!courseOption.equals("X"));
                            break;

                        case "0":
                            System.out.println("Exiting program...");
                            break;

                        default:
                            System.out.println("Invalid option");
                            break;
                    }

        } while (!option.equals("0"));

        scanner.close();
        writeArrayListToFile(listOfLectures);


    }
}



enum LectureType {
    GRAD, UNDERGRAD;
}
enum LectureMode {
    F2F, MIXED, ONLINE;
}
//________________________________________________________________________

class Lab {
    private String crn;
    private String classroom;

    public String getCrn() {
        return crn;
    }
    public void setCrn(String crn) {
        this.crn = crn;
    }
    public String getClassroom() {
        return classroom;
    }
    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }
    @Override
    public String toString() {
        return crn + "," + classroom;
    }
    public Lab(String crn, String classroom) {
        this.crn = crn;
        this.classroom = classroom;
    }
}//end of class Lab
//________________________________________________________________________

class Lecture {
    private String crn;
    private String prefix;
    private String lectureName;
    private LectureType lectureType; //Grad or UnderGrad
    private LectureMode lectureMode; //F2F, Mixed or Online
    private String classroom;
    private boolean hasLabs;
    private int creditHours;
    ArrayList<Lab> labs;
    // _________________

    //Helper method-used in constructors to set up the common fields
    private void LectureCommonInfoSetUp (String crn, String prefix, String lectureName, LectureType lectureType, LectureMode lectureMode) {
        this.crn = crn;
        this.prefix = prefix;
        this.lectureName = lectureName;
        this.lectureType = lectureType;
        this.lectureMode = lectureMode;
    }

    // Non-online with Labs
    public Lecture( String crn, String prefix, String lectureName, LectureType lectureType, LectureMode lectureMode, String classroom, boolean hasLabs, int creditHours, ArrayList<Lab> labs ) {
        LectureCommonInfoSetUp(crn,prefix,lectureName,lectureType,lectureMode);
        this.classroom = classroom;
        this.hasLabs = hasLabs;
        this.creditHours = creditHours;
        this.labs = labs;
    }
    // Constructor for Non-online without Labs
    public Lecture( String crn, String prefix, String lectureName, LectureType lectureType, LectureMode lectureMode, String classroom, boolean hasLabs, int creditHours) {
        LectureCommonInfoSetUp(crn,prefix,lectureName,lectureType,lectureMode);
        this.classroom = classroom;
        this.hasLabs = hasLabs;
        this.creditHours = creditHours;
    }
    // Constructor for Online Lectures
    public Lecture(String crn, String prefix, String lectureName, LectureType lectureType, LectureMode lectureMode, int creditHours) {
        LectureCommonInfoSetUp(crn,prefix,lectureName,lectureType,lectureMode);
        this.classroom = classroom;
        this.hasLabs = hasLabs;
        this.creditHours = creditHours;
    }
    //________
    @Override
    public String toString() {
        String lectureAndLabs = crn + "," + prefix + "," + lectureName + "," + lectureType + "," + lectureMode + "," + hasLabs + "," + creditHours+"\n";
        if ( labs != null ) {//printing corresponding labs
            //lectureAndLabs+="\n";
            for (Lab lab: labs)
                lectureAndLabs += lab +"\n";
        }
        return lectureAndLabs;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLectureName() {
        return lectureName;
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }

    public LectureType getLectureType() {
        return lectureType;
    }

    public void setLectureType(LectureType lectureType) {
        this.lectureType = lectureType;
    }

    public LectureMode getLectureMode() {
        return lectureMode;
    }

    public void setLectureMode(LectureMode lectureMode) {
        this.lectureMode = lectureMode;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }

    public boolean isHasLabs() {
        return hasLabs;
    }

    public void setHasLabs(boolean hasLabs) {
        this.hasLabs = hasLabs;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public ArrayList<Lab> getLabs() {
        return labs;
    }

    public void setLabs(ArrayList<Lab> labs) {
        this.labs = labs;
    }
}
//________________________________________________________________________

class ClassManagement {
    private ArrayList<Lecture> listOfLectures;
    private ArrayList<String> coursesWithStudents;


    public ClassManagement(ArrayList<Lecture> listOfLectures) {
        this.listOfLectures = listOfLectures;
        this.coursesWithStudents = new ArrayList<>();
    }


    // Add methods for managing classes here, such as:
    // - Search for a class or lab using the class/lab number
    // - Delete a class (this requires the deletion of all the labs associated with that class)
    // - Add a lab to a class when applicable

    public void searchClassByCrn(String crn) {
        // First, check labs directly
        for (Lecture lecture : listOfLectures) {
            List<Lab> labs = lecture.getLabs();
            if (labs != null && !labs.isEmpty()) {
                for (Lab lab : labs) {
                    if (lab.getCrn().equals(crn)) {
                        System.out.println("Lab details:");
                        System.out.println("Lab Room: " + lab.getClassroom());
                        System.out.println("Lab for:" +lecture.getCrn() + "," + lecture.getPrefix() + "," + lecture.getLectureName());
                        System.out.println("\n");
                        return;
                    }
                }
            }
        }

        // If not found in labs, check lectures
        for (Lecture lecture : listOfLectures) {
            if (lecture.getCrn().equals(crn)) {
                System.out.println("Lecture details:");
                System.out.println(lecture.getCrn() + "," + lecture.getPrefix() + "," + lecture.getLectureName());
                System.out.println("\n");
                return;
            }
        }
        System.out.println("Class not found with number: " + crn);
    }


    public void deleteClassByCrn(String crn) {
        for (Lecture lecture : listOfLectures) {
            if (lecture.getCrn().equals(crn)) {
                listOfLectures.remove(lecture);
                System.out.println("Class deleted: " + lecture);
                return;
            }
        }
        System.out.println("Class not found with number: " + crn);
    }

    public void addLabByCrn(String crn){
        for(Lecture lecture : listOfLectures){
            if (lecture.getCrn().equals(crn)) {


            }
        }
    }

    public Lecture getLectureByCrn(String crn) {
        for (Lecture lecture : listOfLectures) {
            if (lecture.getCrn().equals(crn)) {
                return lecture;
            }
        }
        return null; // Return null if lecture with given CRN is not found
    }

    public Boolean hasStudents(String crn) {
        for (String courses : coursesWithStudents) {
            if(courses.equals(crn)){
                return true;
            }
        }
        return false;
    }

    public void addCourseWithStudents(String crn) {
        coursesWithStudents.add(crn);
    }


}

//_________________________________________________________________________________

class ClassesFromFile {
    public static ArrayList<Lecture> loadLecturesFromFile() throws FileNotFoundException {
        ArrayList<Lecture> listOfLectures = new ArrayList<>();


        Scanner scanner = new Scanner(new File("lec.txt"));



        String line = "";
        String[] lectureItems;
        Lecture lecture=null;

        boolean skipLine = false;
        boolean oneMorePass = false;

        while (scanner.hasNextLine() || oneMorePass ) {

            if (skipLine == false) {
                line = scanner.nextLine();
            }

            oneMorePass = false;

            lectureItems = line.split(",");

            // --------------------------------------------------------------------
            if (lectureItems.length > 2) {// It must be F2F, Mixed or Online lecture

                LectureType type; // Grad or UnderGrad
                LectureMode mode; // Online, F2F or Mixed
                ArrayList<Lab> labList = new ArrayList<>();

                type = LectureType.GRAD;
                if (lectureItems[3].compareToIgnoreCase("Graduate") != 0)
                    type = LectureType.UNDERGRAD;

                // ________________________________________
                if (lectureItems[4].compareToIgnoreCase("ONLINE") == 0) {
                    skipLine = false;
                    lecture = new Lecture(lectureItems[0], lectureItems[1], lectureItems[2], type, LectureMode.ONLINE,
                            Integer.parseInt(lectureItems[5]));


                } else {

                    mode = LectureMode.F2F;
                    if (lectureItems[4].compareToIgnoreCase("F2F") != 0)
                        mode = LectureMode.MIXED;

                    boolean hasLabs = true;
                    if (lectureItems[6].compareToIgnoreCase("yes") != 0)
                        hasLabs = false;

                    if (hasLabs) {//Lecture has a lab
                        skipLine = true;

                        String[] labItems;
                        while (scanner.hasNextLine()) {
                            line = scanner.nextLine();
                            if (line.length() > 15) {//True if this is not a lab!

                                if ( scanner.hasNextLine() == false ) {//reading the last line if any...
                                    oneMorePass = true;
                                }

                                break;
                            }
                            labItems = line.split(",");
                            Lab lab = new Lab(labItems[0], labItems[1]);
                            labList.add(lab);


                        }//end of while
                        lecture = new Lecture(lectureItems[0], lectureItems[1], lectureItems[2], type, mode, lectureItems[5], hasLabs,
                                Integer.parseInt(lectureItems[7]), labList);

                    } else {//Lecture doesn't have a lab
                        skipLine = false;
                        lecture = new Lecture(lectureItems[0], lectureItems[1], lectureItems[2], type, mode, lectureItems[5], hasLabs,
                                Integer.parseInt(lectureItems[7]));

                    }
                }

            }

            listOfLectures.add(lecture);
        }//end of while
        scanner.close();

        return (listOfLectures);
    }


}


//_________________________________________________________________________________

class StudentManagement {
    private ArrayList<Student> studentList;

    public StudentManagement() {
        this.studentList = new ArrayList<>();
    }

    // Method to add a new student
    public void addStudent(String name, String id, double gpa, boolean resident, ArrayList<Lecture> enrolledCourses) {
        // Create a new UndergraduateStudent object
        UndergraduateStudent newStudent = new UndergraduateStudent(name, id, gpa, resident, enrolledCourses);
        // Add the new student to the list
        studentList.add(newStudent);
    }

    public void addStudent(String name, String id, ArrayList<Lecture> enrolledCourses) {
        // Create a new MsStudent object
        MsStudent newStudent = new MsStudent(name, id, enrolledCourses);
        // Add the new student to the list
        studentList.add(newStudent);
    }

    public void addStudent(String name, String id, String advisor, String researchSubject, ArrayList<Lecture> enrolledCourses) {
        // Create a new PhDStudent object
        PhdStudent newStudent = new PhdStudent(name, id, advisor, researchSubject, enrolledCourses);
        // Add the new student to the list
        studentList.add(newStudent);
    }

    // Method to search for a student by ID
    public Student searchStudentById(String id) {
        for (Student student : studentList) {
            if (student.getId().equals(id)) {
                return student; // Return the student if found
            }
        }
        return null; // Return null if student not found
    }

    // Method to delete a student by ID
    public void deleteStudentById(String id) {
        for (Student student : studentList) {
            if (student.getId().equals(id)) {
                studentList.remove(student); // Remove the student if found
                System.out.println("Student deleted successfully.");
                return;
            }
        }
        System.out.println("Student not found.");
    }

    // Method to print fee invoice for a student by ID
    public void printFeeInvoice(String id) {
        Student student = searchStudentById(id);
        if (student != null) {
            student.printInvoice(); // Print the fee invoice if student found
        } else {
            System.out.println("Student not found.");
        }
    }

    public Boolean isDuplicateId(String id){
        for (Student student : studentList){
            if (student.getId().equals(id)) {
                return true; // Return the student if found
            }
        }
        return false;
    }


    // Method to print list of all students


     public void printListOfStudents() {
     System.out.println("List of Students Grouped by Student Type:");
     // Group students by type
     Map<String, List<String>> studentsByType = new HashMap<>();
     for (Student student : studentList) {
     String studentType;
     if (student instanceof UndergraduateStudent) {
     studentType = "Undergraduate Students";
     } else if (student instanceof MsStudent) {
     studentType = "MS Students";
     } else if (student instanceof PhdStudent) {
     studentType = "PhD Students";
     } else {
     // Handle unknown student types if any
     continue;
     }

     // Add student to the corresponding group
     studentsByType.computeIfAbsent(studentType, k -> new ArrayList<>()).add(student.getName());
     }

     // Print students grouped by type
     for (Map.Entry<String, List<String>> entry : studentsByType.entrySet()) {
     String studentType = entry.getKey();
     List<String> students = entry.getValue();

     // Print header
     System.out.println(studentType);
     System.out.println("------------");

     // Print students
     for (String student : students) {
     System.out.println("- " + student);
     }
     System.out.println(); // Add an empty line after each group
     }
     }

}

class IdException extends Exception {
    public IdException(String message) {
        super(message);
    }
}

abstract class Student {
    private String name;
    private String id;

    public Student(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    abstract public void printInvoice();


}//end of Student class

//_________________________________________________________________________________



class UndergraduateStudent extends Student {
    private double gpa;
    private boolean resident;
    private ArrayList<Lecture> enrolledCourses;

    public UndergraduateStudent(String name, String id, double gpa, boolean resident, ArrayList<Lecture> enrolledCourses){
        super(name, id);
        this.gpa = gpa;
        this.resident = resident;
        this.enrolledCourses = new ArrayList<>(enrolledCourses);
    }



    @Override
    public void printInvoice(){
        double totalPayment = 35.00;
        double totalPerCrn;

        System.out.println("\nVALENCE COLLEGE");
        System.out.println("ORLANDO FL 10101");
        System.out.println("------------------------");

        System.out.println("\nFee Invoice Prepared for Student:");
        System.out.println((getId() + "-" + getName()).toUpperCase());

        System.out.println("\n1 Credit Hour = $120.25");

        System.out.println("\nCRN\t\t\tCR_PREFIX\t\tCR_HOURS");
        for(Lecture course : enrolledCourses){
            String crn = course.getCrn();
            String prefix = course.getPrefix();
            int creditHours = course.getCreditHours();

            if(resident) {
                totalPerCrn = creditHours * 120.25;
            }else {
                totalPerCrn = creditHours * 240.5;
            }

            System.out.printf("%s\t\t%s\t\t\t%d\t\t\t\t$ %.2f%n\n", crn, prefix, creditHours,totalPerCrn);
            totalPayment += totalPerCrn;
        }

        System.out.println("                           Health & id fees $ 35.00\n");

        System.out.println("-------------------------------------------------------");


        //applying discount
        if(gpa >= 3.5 && totalPayment > 500 && resident){
            double discount = totalPayment * .25;
            double fTotal = totalPayment - discount;
            System.out.printf("                                            $ %.2f%n", totalPayment);
            System.out.printf("                                           -$ %.2f%n", discount);
            System.out.println("                                       ----------------");
            System.out.printf("                           Total Payments   $ %.2f%n", fTotal);
        }else {
            System.out.printf("                           Total Payments   $ %.2f%n", totalPayment);
        }

    }


}// end of undergradstudent class

//_________________________________________________________________________________


abstract class GraduateStudent extends Student {

    public GraduateStudent (String name, String id){
        //crn is the crn that the grad student is a ta for
        super(name,id);
    }

}// end of gradstudent class

//_________________________________________________________________________________

class PhdStudent extends GraduateStudent {
    private String advisor;
    private String researchSubject;
    private ArrayList<Lecture> enrolledCourses;


    public PhdStudent (String name, String id, String advisor, String researchSubject, ArrayList<Lecture> enrolledCourses){
        super(name, id);
        this.advisor = advisor;
        this.researchSubject = researchSubject;
        this.enrolledCourses = new ArrayList<>(enrolledCourses);
    }

    public String getResearchSubject() {
        return researchSubject;
    }

    public void setResearchSubject(String researchSubject) {
        this.researchSubject = researchSubject;
    }

    public String getAdvisor() {
        return advisor;
    }

    public void setAdvisor(String advisor) {
        this.advisor = advisor;
    }

    public ArrayList<Lecture> getEnrolledCourses() {
        return enrolledCourses;
    }

    public void setEnrolledCourses(ArrayList<Lecture> enrolledCourses) {
        this.enrolledCourses = enrolledCourses;
    }

    @Override
    public void printInvoice(){
        double totalPayment = 35.00 + 700;
        int crns = enrolledCourses.size();

        System.out.println("\nVALENCE COLLEGE");
        System.out.println("ORLANDO FL 10101");
        System.out.println("------------------------");

        System.out.println("\nFee Invoice Prepared for Student:");
        System.out.println((getId() + "-" + getName()).toUpperCase());

        System.out.println("\nRESEARCH");

        if (crns == 2) {
            double ftotal = totalPayment * .5;
            System.out.println(getResearchSubject() + "                            $ -"+ftotal);

            System.out.println("                           Health & id fees $ 35.00\n");

            System.out.println("-------------------------------------------------------");
            System.out.printf("                                 Total Payments   $ %.2f%n", ftotal);
        } else if (crns >= 3 ) {
            double ftotal = totalPayment - 700;
            System.out.println(getResearchSubject() + "                                  $ -700.00");

            System.out.println("                           Health & id fees $ 35.00\n");

            System.out.println("-------------------------------------------------------");
            System.out.printf("                           Total Payments   $ %.2f%n", ftotal);
        } else {
            System.out.println(getResearchSubject() + "                                   $ 700.00");

            System.out.println("                           Health & id fees $ 35.00\n");

            System.out.println("-------------------------------------------------------");
            System.out.printf("                           Total Payments   $ %.2f%n", totalPayment);
        }


    }

}// end of phdstudent class

//_________________________________________________________________________________

class MsStudent extends GraduateStudent {
    private ArrayList<Lecture> enrolledCourses;

    public MsStudent(String name, String id, ArrayList<Lecture> enrolledCourses) {
        //gradCoursesTaken is the array of the crns that the Ms student is taking
        //crn is the course number that the PhD student is a ta for
        super(name, id);
        this.enrolledCourses = new ArrayList<>(enrolledCourses);
    }

    @Override
    public void printInvoice() {
        double totalPayment = 35.00;


        System.out.println("\nVALENCE COLLEGE");
        System.out.println("ORLANDO FL 10101");
        System.out.println("------------------------");

        System.out.println("\nFee Invoice Prepared for Student:");
        System.out.println((getId() + "-" + getName()).toUpperCase());

        System.out.println("\n1 Credit Hour = $300.00");

        System.out.println("\nCRN\t\t\tCR_PREFIX\t\tCR_HOURS");
        for (Lecture course : enrolledCourses) {
            int creditHours = course.getCreditHours();
            String prefix = course.getPrefix();
            String crn = course.getCrn();
            double totalPerCrn = creditHours * 300;
            System.out.printf("%s\t\t%s\t\t\t%d\t\t\t\t$ %.2f%n\n", crn, prefix, creditHours, totalPerCrn);
            totalPayment += totalPerCrn;
        }

        System.out.println("                           Health & id fees $ 35.00\n");

        System.out.println("-------------------------------------------------------");
        System.out.printf("                           Total Payments   $ %.2f%n", totalPayment);

    }
}



//_________________________________________________________________________________


