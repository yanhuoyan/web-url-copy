# Web URL Copy

An IntelliJ IDEA plugin that helps developers quickly generate HTTP requests from Java methods and copy them to the clipboard.

[简体中文](README.zh.md) | English

## Version 2024.3.5 Update Notes

To address memory leak issues in IntelliJ IDEA 2024.3.5, the following changes have been made:

1. Added `DisposerUtil` utility class for proper handling of Disposable objects
2. Modified `ConfigToolWindowPanel` class to implement Disposable interface and correctly manage resource disposal

## Features

- **Bash Curl Commands**: Generate and copy curl commands directly from your Java controller methods
- **Python Requests Code**: Generate and copy Python requests code with proper formatting
- **Multiple API Styles Support**: Works with both Spring MVC and JAX-RS style APIs
- **Custom Host Configuration**: Set default host addresses for your environments (dev, test, prod)
- **Request Headers Management**: Add, edit, and delete custom HTTP headers for your requests
- **Method Parameter Handling**: Automatically detects and includes path variables, query parameters, and request body
- **HTTP Method Recognition**: Automatically recognizes GET, POST, PUT, DELETE and other HTTP methods from annotations
- **Context-Aware Menu**: Right-click menu options are only shown for compatible Java methods
- **Clipboard Integration**: One-click copying of generated requests

## How to Use

1. Open a Spring MVC or JAX-RS controller class in the editor
2. Right-click on a method name or class name
3. Select "Web Copy URL" > "Copy Bash Curl" or "Copy Python Request" from the context menu
4. The request code will be copied to your clipboard
5. Configure request addresses and headers through the "Web URL Config" tool window on the right side of the IDE

## Configuration

Through the "Web URL Config" tool window on the right side of the IDE, you can:

- Set default host addresses for different environments
- Add/remove/edit custom request headers
- Configure default content types for requests
- Set authentication tokens or credentials if needed

## Supported Frameworks

- **Spring MVC**: Supports @RestController, @Controller, @RequestMapping, @GetMapping, @PostMapping, @PutMapping, @DeleteMapping annotations
- **JAX-RS**: Supports javax.ws.rs.Path, @GET, @POST, @PUT, @DELETE annotations and path parameters

## Example Generated Output

### Curl Command Example
```bash
curl -X POST 'http://localhost:8080/api/users' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer token' \
  -d '{
    "name": "John Doe",
    "email": "john@example.com"
  }'
```

### Python Request Example
```python
import requests
import json

url = "http://localhost:8080/api/users"

payload = json.dumps({
  "name": "John Doe",
  "email": "john@example.com"
})

headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer token'
}

response = requests.post(url, headers=headers, data=payload)
print(response.text)
```

## Building from Source

```bash
./gradlew buildPlugin
```

## Installation

- **Local Installation**: Download the latest release or build it yourself, then install the plugin from disk in IntelliJ IDEA
- **Plugin Marketplace**: Search for "Web URL Copy" in the IntelliJ IDEA plugin marketplace

## Development Requirements

- Java 17+
- IntelliJ IDEA (2022.2 - 2024.3.5)

## License

[MIT License](LICENSE)