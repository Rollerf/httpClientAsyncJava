# Peticiones HTTP asincronas en Java
## Introducción
En esta demo hago se presenta un workaround para realizar peticiones HTTP asincronas con HttpClient de Java.
Esto viene dado por una curiosidad mia personal de explorar como manejar peticiones asincronas en Java.

## Descrición de la prueba
La prueba consiste en hacer tres peticiones HTTP get a la API de [jsonplaceholder](https://jsonplaceholder.typicode.com/).
- Una petición para obtener los TODOs de un usuario
- Una petición para obtener los albums de un usuario
- Una petición para obtener los posts de un usuario
    - Dentro de cada post se hace una petición para obtener los comentarios

Esto se englobará dentro de un objeto que representará un usuario.
La estructura JSON que devolveremos tras la recuperación de los datos será la siguiente:
```json
{
    "albums": [
        {
            "userId": 1,
            "id": 1,
            "title": "quidem molestiae enim"
        },
    ]
    "posts": [
        {
            "userId": 1,
            "id": 1,
            "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
            "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto",
            "comments": [
                {
                    "postId": 1,
                    "id": 1,
                    "name": "id labore ex et quam laborum",
                    "email": "Eliseo@gardner.biz",
                    "body": "laudantium enim quasi est quidem magnam voluptate ipsam eos\ntempora quo necessitatibus\ndolor quam autem quasi\nreiciendis et nam sapiente accusantium"
                },
            ]
        },
    ],
    "todos": [
        {
            "userId": 1,
            "id": 1,
            "title": "delectus aut autem",
            "completed": false
        },
    ]
}
```
## Comandos para arrancar el proyecto:
```bash
mvn clean install
quarkus dev
```
Tanto maven como quarkus se pueden instalar desde [sdkman](https://sdkman.io/)

## Pruebas
Para probar el proyecto se puede hacer una petición GET a la siguiente URL:
```bash
http://localhost:8080/example/requestTest/1
```
Donde el número 1 es el id del usuario que queremos recuperar.