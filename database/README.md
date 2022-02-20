# Calibrated Peer Review Demo

## Getting Started

This web application is built on the production environment using Docker images. The guide will contain 2 sections, aimed more heavily toward the Docker deployment on Linux environment.

## Running the Project

This guide will assume that [Docker](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/) are already installed on your machine.

**Step 1:** Clone the repository.

**Step 2:** At the root of the directory, create a `.env` file using `.env.example` as the template to fill out the variables.

**Step 3:** Update the server name and localhost port value in the `nginx.conf` file to set up a reverse proxy and make sure the listening port matches the one that is mapped from in `docker-compose.yml` (`80` by default).

**Step 4:** Create the Docker image and start the containers by running `docker-compose -f "docker-compose.yml" up -d --build` in the same directory.

## Local Development Environment

For the local development environment, a Docker setup is not necessary. This guide assumes you already have [Maven](https://maven.apache.org/guides/getting-started/windows-prerequisites.html) 3.8.4 or higher and [JDK 17](https://openjdk.java.net/projects/jdk/17/) or higher installed. For Windows users:

**Step 1:** Install [MongoDB for Windows](https://docs.mongodb.com/manual/tutorial/install-mongodb-on-windows/#install-mongodb-community-edition) (or Mac and Linux counterparts respectively if you are using one). Optionally, install [MongoDB Compass](https://www.mongodb.com/products/compass) and [mongosh](https://docs.mongodb.com/mongodb-shell/) for your own convenience of executing shell commands.

**Step 2:** On Windows, hit `Windows + R`, enter `sysdm.cpl` and navigate to `Advanced -> Environment Variables...` and add the following variables:
- `MONGO_HOSTNAME`: `localhost`
- `MONGO_PORT`: `27017`
- `MONGO_DATABASE`: `cpr`
- `MONGO_USERNAME`: `<your choice>`
- `MONGO_PASSWORD`: `<your choice>`

**Step 3:** Using the MongoDB shell (either using MongoDB shell from terminal or the built-in one in MongoDB Compass), create authentication for the database, run the following command:

- `db.createUser({user: "<your username>", pwd: "<your password>", roles: [{role: "readWrite", db: "cpr"}]});`

**Step 4:** Run `mvn liberty:dev` to start the project in developer mode. The web app should be running on http://localhost:13126 (the port depends on which microservice you are running).

## Contributing

Contributors are more than welcome to improve the project by creating a new issue to report bugs, suggest new features, or make changes to the source code by making a pull request. To have your work merged in, please make sure the following is done:

1. Fork the repository and create your branch from master.
2. If you’ve fixed a bug or added something new, add a comprehensive list of changes.
3. Ensure that your code is tested, functional, and is linted.

## Built On

This project is built on:

- [Docker](https://www.docker.com/)
- [Java](https://openjdk.java.net/)
- [Maven](https://maven.apache.org/)
- [MongoDB](https://www.mongodb.com/)
- [Nginx](https://www.nginx.com/)
- [Open Liberty](https://openliberty.io/)
- [ReactJS](https://reactjs.org/)


