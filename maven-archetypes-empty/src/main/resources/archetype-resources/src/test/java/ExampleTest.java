package ${groupId};

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class ExampleTest {

	@Test
	public void testProject() throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
		properties.load(this.getClass().getClassLoader().getResourceAsStream("application-test.properties"));
		assertEquals("hello", properties.get("greeting"));
		assertEquals("true", properties.get("test"));
	}
}
