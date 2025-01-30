package ${groupId}.api;

/**
 * A very loose interpretation of the JSON API Spec. (Work in progress)
 *
 * https://jsonapi.org/
 */
public class Data<T> {

	private T data;

	protected Data(T data) {
		this.data = data;
	}

	public static <T> Data<T> from(T data) {
		return new Data<T>(data);
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
}
