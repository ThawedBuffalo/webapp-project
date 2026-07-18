import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/CourseSearchServlet")
public class CourseSearchServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Read values from the HTML form
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String keyword = request.getParameter("keyword");

        // List of available courses
        String[] courses = {
                "CSC 1300 - Introduction to Programming",
                "CSC 1800 - Data Structures",
                "CSC 3200 - Computer Networks",
                "CSC 3300 - Operating Systems",
                "CSC 4200 - Database Systems"
        };

        // Prepare HTML output
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<html>");
        out.println("<head><title>Course Search Results</title></head>");
        out.println("<body>");

        out.println("<h2>Welcome " + firstName + " " + lastName + "</h2>");

        out.println("<h3>Search Keyword:</h3>");
        out.println("<p>" + keyword + "</p>");

        out.println("<h3>Matching Courses</h3>");

        boolean found = false;

        // Search every course
        for (String course : courses) {

            if (course.toLowerCase().contains(keyword.toLowerCase())) {

                out.println(course + "<br>");

                found = true;
            }
        }

        // If no courses matched
        if (!found) {
            out.println("<p>No matching courses were found.</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }
}