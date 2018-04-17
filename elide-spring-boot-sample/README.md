# elide-spring-boot-sample

### Play with Postman 

Import the [`postman_collection.json`](postman_collection.json).

### Play with JavaScript

- CREATE / POST

  ```javascript
  var data = {
    "data": {
      "type": "user",
      "attributes": {
        "username": "test",
        "password": "test"
      }
    }
  };
  fetch('http://localhost:8080/api/user', {
    method: 'POST',
    headers: {
      'Accept': 'application/vnd.api+json',
      'Content-Type': 'application/vnd.api+json',
    },
    body: JSON.stringify(data),
  })
  .then(response => response.ok && response.json())
  .then(json => console.log(json));
  ```

- QUERY / GET

  ```javascript
  fetch('http://localhost:8080/api/user?page[number]=1&page[size]=3&page[totals]', {
    method: 'GET',
    headers: {
      'Accept': 'application/vnd.api+json',
    },
  })
  .then(response => response.ok && response.json())
  .then(json => console.log(json));
  ```

- UPDATE / PATCH

  ```javascript
  var data = {
    "data": {
      "id": "1",
      "type": "user",
      "attributes": {
        "username": "test",
        "encodedPassword": "test"
      }
    }
  };
  fetch('http://localhost:8080/api/user/1', {
    method: 'PATCH',
    headers: {
      'Accept': 'application/vnd.api+json',
      'Content-Type': 'application/vnd.api+json',
    },
    body: JSON.stringify(data),
  })
  .then(response => console.log(response.status === 204 ? 'ok' : 'failure'));
  ```

- DELETE / DELETE

  ```javascript
  fetch('http://localhost:8080/api/user/1', {
    method: 'DELETE',
    headers: {
      'Accept': 'application/vnd.api+json',
    },
  })
  .then(response => console.log(response.status === 204 ? 'ok' : 'failure'));
  ```

### Next 

More information about Elide can be found at [elide github repo](https://github.com/yahoo/elide) and [elide.io](http://elide.io/).