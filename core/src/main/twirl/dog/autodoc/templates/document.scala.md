@(title: String, description: Option[String], request: RequestDocument, response: ResponseDocument)
## @title@description.map("\n"+_).getOrElse("")

#### Request
```
@request.method @request.path@request.headers@request.body
```

#### Response
```
@response.status@response.headers@response.body
```
