package ${groupId}.hello;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import ${groupId}.util.sql.AppDataSource;

@WebServlet(urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

	private static final long serialVersionUID = 3892187441638408457L;

	@Inject
	@AppDataSource
	DataSource dataSource;

	@PersistenceContext
	EntityManager entityManager;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("Hello....");
		testDataSource();
		request.getRequestDispatcher("/WEB-INF/views/main/hello.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void testDataSource() {
		if (dataSource != null) {
			String validationSql = "SELECT 1 FROM SYSIBM.SYSDUMMY1";
			try (Connection connection = dataSource.getConnection();
					PreparedStatement statement = connection.prepareStatement(validationSql);
					ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					int value = resultSet.getInt(1);
					System.out.println(String.format("Data source is up (%d)", value));
				}
			} catch (SQLException ex) {
				// status will be DOWN
				ex.printStackTrace();
			}
		} else {
			System.out.println("No datasource");
		}
		if (entityManager != null) {
			System.out.println("EntityManager found");
		} else {
			System.out.println("No entity manager");
		}
	}
}
