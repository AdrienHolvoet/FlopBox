# Application «FlopBox»

Adrien Holvoet      
27/03/2021
## Introduction
Cette application adopte le style architectural REST pour permettre de centraliser la gestion de fichiers distants stockés dans des serveurs FTP tiers. L'api JAX-RS est utlisée pour mettre en oeuvre ce logiciel REST.
## Comment l'utiliser ?

La javadoc a déjà été générée, elle est placée dans le dossier **doc/javadocs**, il est possible de la regénérer dans le dossier target qui se créera automatiquement.  
Pour générer la Javadoc :  
```
mvn javadoc:javadoc      
```
L'exécutable a déjà été généré et il se trouve à la racine du projet.
Il suffit donc de se placer dans le projet FlopBox et de lancer la commande :

```
java -jar FlopBox-jar-with-dependencies.jar
```

L'application sera sous écoute sur le port 8080 sous l'adresse suivante : http://localhost:8080/flopbox/   
Voir la partie Collection Postman(Voir dernier point) pour connaître l'ensemble des ressources/endpoints disponibles.

## Demo
**Démo** :  On retrouve à la la racine du projet le fichier demo.mkv est une vidéo démo d'utilisation de l'application mettant en relief l'ensemble des endpoints utilisés. Cette démo utilise la librairie pyftpdlib pour simuler un serveur FTP local. 
## Architecture

Cette application respecte le style archictetural REST pour créer des services web. L'architecture logicielle MVC est utilisée pour la structure du projet. En effet, on retrouve la partie **Model** représentant la logique de l'application dans le package *model* et *service*.
On retrouvre également la partie **Controller** dans le package *controller* qui gère les requêtes des utilisateurs. Elle est responsable de retourner une réponse au client à l'aide la couche métier. On ne retrouve pas vraiment la partie **Vue** ici car seul du JSON ou des bytes sont retournés au client.

### Classes/Interfaces  
- Dans le package **common** : 
    - *CheckUtils.java* :  Classe (Singleton pattern) qui contient toutes les méthodes de vérification des paramètres via l'API FlopBox
    - *Command.java* : Enumération représentant les commandes FTP implémentées
    - *Constants.java* : Classe (Singleton pattern) qui contient toutes les constantes utilisées dans l'application  
  
- Dans le package **controller** (Gère les requêtes HTTP) : 
    - *AuthenticationResource.java* : la ressource d'authentification permettant au client de se connecter et d'utiliser la plateforme( créer, mettre à jour et supprimer un serveur disponible). Le reste étant accessible à tous.
    - *FtpResource.java* : la ressource permettant au client d'accéder à des serveurs FTP enregistrés sur la plateforme et d'opérer des actions spécifiques au protocole FTP
    - *ServerResource.java* : La ressource permettant au client de gérer les serveurs ftp accessible depuis l'application FlopBox

- Dans le package **exception** : 
    - *HandleException.java* : Classe utilisée pour gérer les exceptions majeures / code d'erreur avec des messages personnalisés dans un emplacement centralisé. Ils seront envoyés à l'utilisateur pour l'avertir que quelque chose s'est mal passé.
    - *NotAuthorizedException.java* :  Classe caractérisant une exception lorsque la demande n'a pas été appliquée car elle ne dispose pas d'informations d'authentification valides pour la ressource cible.
    - *UnexpectedFtpStatusCodeException.java* : Classe caractérisant une exception lorsqu'un code d'état inattendu est envoyé par le serveur ftp.

- Dans le package **filter** : 
    - *Authorize.java* : Interface caratérisant Une annotation qui se lie à un filtre. Les endpoints qui auront cette annotation ne peuvent être appelés que si l'appelant est authentifié.
    - AuthorizeFilter.java : Cette classe contient la logique de l'annotation. Celle-ci vérifie que si le bearer token contenu dans le header est correct ou non.

- Dans le package **model** (l'ensemble des POJO) : 
    - *Credentials.java* : Classe qui représente les identifiants d'un utilisateur de la plateforme flopbox,
    - *ErrorResponse.java* : Classe qui représente un message d'erreur envoyé au client lorsque le serveur ne peut pas répondre à la demande.
    - *JwtToken.java* : Classe qui représente un jeton jwt.
    - *Server.java* : Classe qui représente un serveur ftp(adresse,port,nom).

- Dans le package **service** : 
    - *AuthenticationService.java* : Classe Singleton qui contient toute la logique liée à l'authentification avec bearer token
    - *FtpService.java* : Classe qui contient toute la logique liée à la ressource ftp. Ce service exécutera toutes les commandes liées à un serveur ftp. Implémente Thread pour permettre plusieurs connexions simultanées à des serveurs ftp.
    - *JwtTokenService.java* : Classe singleton chargée d'analyser et générer le jeton un jwt token depuis une instance de *Credentials*
    - *ServerService.java* : Classe Singleton qui contient toute la logique liée à la ressource Serveur utilisée pour effectuer les opérations CRUD.

### Gestion d'erreur

La gestion d'erreur se fait grâce à la classe *HandleException* du package **exception**. En effet celle-ci permet de centraliser la gestion d'exception. Toutes les exceptions seront catch depuis le plus haut niveau de l'application (controllers) et seront envoyées vers cette classe ne contenant qu'une seule méthode. Celle-ci s'occupera en fonction de l'exception reçue et du controller d'envoyer un message d'erreur personnalisé au client et de log l'erreur.
Toute exception non attendue déclenchera une erreur 500 avec le message de l'exception qui sera retourné au client.

#### Catch

Toutes les exceptions sont donc catch dans chaque endpoint des différentes ressources. Ces catch sont de cette forme : 
```
try {
    ...logique/service correspondant à la requête utilisateur
    ...return 200 code si tout va bien
}
catch (Exception e) {
	return HandleException.handleException(e, logger);
}
```
Le catch redirige donc toutes les exceptions vers la classe *HandleException*.

On retrouve également un try catch dans le filtre *AuthorizeFilter* qui renverra un code 401 au client si quelque chose se passe mal lors de la vérification du bearer token.

#### Throw

- Dans **Main.java** : 
    - Throws *IOException* : lancée quand le serveur ne peut pas démarrer correctement.

- Dans **FtpRessource.java** : 
    - Throws *ftpService.getException()*;  : lancée quand une exception a été levé lors de l'exécution de la logique dans le thread lié à la requête client.

- Dans **Authorize.java** : 
    - Throws *NotAuthorizedException* : lancée quand le bearer token n'est pas renseigné dans le header.

- Dans **AuthentificationService.java** : 
    - Throws *NotAuthorizedException* : lancée quand les identifiants ne sont pas corrects.
    - Throws *FileNotFoundException* : lancée quand le fichier contentant les utilisateurs n'existe pas.
    - Throws *BadRequestException* : lancée quand les identifiants de l'utilisateur ne sont pas renseignés de la bonne manière.


- Dans **ServerService.java** : 
    - Throws *IOException* : lancée lorsque une erreur se produit lors de la lecture du fichier contenant les serveurs.
    - Throws *NotFoundException* : lancée quand le serveur demandé n'existe pas.
    - Throws *BadRequestException* : lancée quand les paramètres renseignés par l'utilisateur ne sont pas conformes.

- Dans **CheckUtils** : 
    - Throws *IOException* : lancée lorsque une erreur d'E / S se produit (Erreur 500).

- Dans **FtpService.java** : 
    - Throws *IOException* : lancée lorsque une erreur d'E / S se produit lors de la lecture des serveurs ou lors des tests si les fichiers locaux existent.(Erreur 500). 
    - Throws *NotFoundException* : lancée quand le serveur demandé n'existe pas ou quand les chemins des fichiers/dossiers distants/locales n'existent pas.
    - Throws *BadRequestException* : lancée quand les paramètres renseignés par l'utilisateur ne sont pas conformes.
    - Throws *UnexpectedFtpStatusCodeException* : lancée quand une réponse d'un serveur ftp n'est pas un code de succès.
    - Throws *ForbiddenException* : lancée quand l'utilisateur n'a pas assez de droits pour effectuer l'opération demandée ( comme upload un fichier en anonyme).
    - Throws *NotAllowedException* : lancée quand le serveur ne peut pas switch en mode passif.
    - throws *NotAuthorizedException* : lancée quand les identifiants ne sont pas corrects pour se connecter au serveur ftp.

## Exigences 

- [x] ENR | Je peux enregistrer un nouveau serveur FTP dans la plate-forme FlopBox  
- [x] SUP | Je peux supprimer un serveur FTP de la plate-forme FlopBox  
- [x] REN | Je peux me modifier une association FTP avec la plate-forme FlopBox 
- [x] CON | Je peux me connecter à un serveur FTP enregistré via la plate-forme FlopBox 
- [x] BADU | La plate-forme rejette ma connexion si mon utilisateur est inconnu 
- [x] BADP | La plate-forme rejette ma connexion si mon mot de passe est incorrect 
- [x] LIST | Je peux lister le contenu d'un répertoire stocké dans un serveur FTP distant 
- [x] GETT | Je peux récupérer un fichier texte via la plate-forme FlopBox 
- [x] GETB | Je peux récupérer un fichier binaire (image) via la plate-forme FlopBox 
- [x] GETR | Je peux récupérer un répertoire complet via la plate-forme FlopBox 
- [x] PUTT | Je peux stocker un fichier texte dans un serveur FTP enregistré 
- [x] PUTB | Je peux stocker un fichier binaire (e.g., image) dans un serveur FTP enregistré 
- [x] PUTR | Je peux stocker un répertoire complet dans un serveur FTP enregistré 
- [x] RENF | Je peux renommer un fichier distant via la plate-forme FlopBox 
- [x] MKD | Je peux créer un répertoire distant via la plate-forme FlopBox 
- [x] REND | Je peux renommer un répertoire distant via la plate-forme FlopBox 
- [x] RMD | Je peux supprimer un répertoire distant via la plate-forme FlopBox 
- [x] CLOS | Je peux couper proprement la connexion (sans crasher les serveurs) 
- [x] PORT | Je peux spécifier le port du serveur FTP associé à la plate-forme 
- [x] URL | Je peux configurer l'URL du serveur FTP associé à la plate-forme 
- [x] ACPA | Le plate-forme FlopBox supporte les modes ACTIF et PASSIF 
- [x] THRE | La plate-forme supporte la connexion de plusieurs clients simultanés 
- [x] CMP | Le code de la plate-forme compile correctement avec Maven  
- [x] DOC | Le code de la plate-forme est documenté (Readme.md, Javadoc) 
- [x] TST | Le code de la plate-forme est proprement testé (tests unitaires sous JUnit) 
- [x] COO | Le code de la plate-forme est suit les principes de conception objet 
- [x] EXE | Le code de la plate-forme FlopBox s'exécute 
- [x] Un mécanisme d'authentification adéquat tenant compte des différentes clés d'authentification envisageables (celles des différents serveurs FTP auquel on peut avoir accès, ainsi que celles de la plate-forme FlopBox).

## Code samples

### Extrait 1 

Unique méthode de la classe HandleException permettant de centraliser la gestion des erreurs et l'envoie des réponses d'erreurs au client. Si une erreur n'est pas attendu, un code 500 est envoyé.
```
    public static final Response handleException(Exception e, Logger logger) {

		// if an unknown error occurs throws a 500 error
		Status status = Response.Status.INTERNAL_SERVER_ERROR;
		String message = "Something went wrong. Please try again later";
		if (e instanceof BadRequestException) {
			status = Response.Status.BAD_REQUEST;
			message = e.getMessage();
		}
		if (e instanceof NotFoundException) {
			status = Response.Status.NOT_FOUND;
			message = e.getMessage();
		}
		if (e instanceof NotAuthorizedException) {
			status = Response.Status.UNAUTHORIZED;
			message = e.getMessage();
		}
		if (e instanceof ForbiddenException) {
			status = Response.Status.FORBIDDEN;
			message = e.getMessage() + " You don't have enough Permission";
		}

		if (e instanceof NotAllowedException) {
			status = Response.Status.METHOD_NOT_ALLOWED;
			message = e.getMessage() + " You don't have enough Permission";
		}

		logger.error("Status : " + status + ", Message : " + e.toString());
		return Response.status(status).entity(new ErrorResponse(status.getStatusCode(), message.replace("\r\n", "")))
				.build();
	}
```

### Extrait 2

Methode exécutée par un thread FtpService. Qui à chaque requête utilisateur va  initier la connection au serveur, se connecter avec les identifiants renseignés et effectuer la commande ftp demandée.

```  
    public void executeCommand(String alias, String credentials, Command command, String args0, String args1, String mode) throws IOException, UnexpectedFtpStatusCodeException, NotAuthorizedException {
        this.connect(alias);
        // switch passive mode if the query param = passive, let the default active mode
        // otherwise
        if (mode != null && mode.equals(Constant.PASSIVE)) {
            this.passiveMode();
        }
        this.login(credentials);
        args0 = File.separator + args0;
        switch (command) {
            case LIST:
                resultObject = this.list(args0);
                break;
            case GETF:
                if ((checkFileExists(args0))) {
                    resultObject = this.getF(args0);
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case GETD:
                if (checkDirectoryExists(args0) && CheckUtils.checkIfLocalRepositoryExist(args1)) {
                    this.getD(args0, "", args1);
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case PUT:
                if (args1 == null) {
                    throw new BadRequestException("Query param cannot be null");
                }
                File file = new File(args1);
                if ((checkDirectoryExists(args0)) && CheckUtils.checkIfLocalFileExist(file)) {
                    resultObject = this.putF(args0, args1);
                } else if (checkDirectoryExists(args0) && file.exists()) {
                    String dir = new File(args0).getAbsolutePath();
                    this.ftpClient.makeDirectory(dir + File.separator + file.getName());
                    this.putD(dir + File.separator + file.getName(), args1, "");
                } else {
                    throw new NotFoundException("One of the specified path doesn't exist");
                }
                break;
            case REN:
                resultObject = this.rename(args0, args1);
                break;
            case MKD:
                resultObject = this.createRepository(args0);
                break;
            case RMD:
                this.deleteDirectory(args0);
                break;
            default:
                break;
        }
        Thread.currentThread().interrupt();
    }

```  
### Extrait 3

Filtre exécuté à chaque appel d'endpoint ayant l'annotation *Authorize*. Celui-ci va récuperer le jwt contenu dans le header Authorization, le décoder et vérifier si l'utilisateur existe. Si quoi que ce soit se passe mal, il empêche l'utilisation du endpoint en lançant un code d'erreur 401.
```
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		try {
			// Get the HTTP Authorization header from the request
			String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
			if (authorizationHeader == null || !(authorizationHeader.startsWith("Bearer"))) {
				throw new NotAuthorizedException("WWW-Authenticate=Bearer");
			}

			// Extract the token from the HTTP Authorization header
			String token = authorizationHeader.substring("Bearer".length()).trim();

			Credentials credentials = jwtTokenService.decodeJWT(token);

			// Throws a exception if the user doesn't exist
			authenticationService.checkIfUserExist(credentials);

		} catch (Exception e) {
			logger.error("Status : " + Response.Status.UNAUTHORIZED + ", Message : " + e.toString());
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), e.getMessage())).build());
		}
	}
```

### Extrait 4
Méthode effectuée à chaque requête utilisateur sur **FtpResource** qui va décoder les identifiants passés dans l'en-tête Authorization: Basic. Connecte l'utilisateur en anonyme si l'entête est vide. Envoie un code 401 si l'utilisateur n'est pas connu

```
    private void login(String credentials) throws NotAuthorizedException {

        try {
            if (!CheckUtils.checkIfStringIsNull(credentials)) {
                this.ftpClient.login(Constant.ANONYMOUS, Constant.ANONYMOUS);
            } else {
                if (!credentials.startsWith("Basic")) {
                    throw new NotAuthorizedException("WWW-Authenticate=Basic");
                }
                String[] tokens = (new String(Base64.getDecoder().decode(credentials.split(" ")[1]), "UTF-8"))
                        .split(":");

                final String username = tokens[0];
                final String password = tokens[1];
                this.ftpClient.login(username, password);
            }
        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new NotAuthorizedException("The credentials are wrong");
        }

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
            throw new NotAuthorizedException("FTP : " + ftpClient.getReplyString());
        }
    }
```
### Extrait 5


Méthode synchronized permettant à un seul thread de mettre à jour un serveur sur la base de données (fichier). Comme ça c'est sûr que deux personnes ne peuvent pas mettre à jour le même serveur en même temps (éviter les conflits) 

```
	public synchronized Server updateServer(String alias, Server server) throws IOException {
		if (!CheckUtils.checkIfStringIsNull(server.getAlias())
				|| !CheckUtils.checkIfStringIsNull(server.getAddress())) {
			throw new BadRequestException("Params cannot be null");
		}

		if (!CheckUtils.checkIfStringIsInCorrectFormat(server.getAlias())
				|| !CheckUtils.checkIfStringIsInCorrectFormat(server.getAddress())) {
			throw new BadRequestException("Params cannot contain ' " + Constant.SEPARATOR + " '");
		}

		final Path path = Paths.get(Constant.SERVER_DB);
		List<String> fileContent = new ArrayList<>(Files.readAllLines(path, StandardCharsets.UTF_8));

		Server oldServer = getServer(alias);
		for (int i = 0; i < fileContent.size(); i++) {
			if (fileContent.get(i).equals(convertServerToTuple(oldServer))) {
				fileContent.set(i, convertServerToTuple(server));
				Files.write(path, fileContent, StandardCharsets.UTF_8);
				return server;
			}
		}
		throw new NotFoundException("The server requested doesn't exist on the FlopBox application");
	}
```
## Collection Postman 

### AuthenticationResource

Authentication resource (exposed at "authentication" path) which allow a user to generate a token if it is credential are good and therefore allow the use of resources with the Authorize annotation



--------


#### 1. POST autenticate


Generate a token for the existing credential passed in parameter and allow him to use it to create / update / delete a server from FlopBox API


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: {{base_url}}/authentication
```



***Body:***

```js        
{
    "username" : "root",
    "password" : "root"
}
```



---


## ServerResource

Server resource (exposed at "server" path) which allow to get all (GET), get (GET), create (POST), update( PUT) and delete (DELETE) a server

### Indices

  * [createServer](#1-createserver)
  * [deleteServer](#2-deleteserver)
  * [getServer](#3-getserver)
  * [getServers](#4-getservers)
  * [updateServer](#5-updateserver)


--------

### 1.POST createServer


Create a new server 


***Endpoint:***

```bash
Method: POST
Type: RAW
URL: {{base_url}}/server
```



***Body:***

```js        
{
    "alias" : "pyftp",
    "address" : "0.0.0.0",
    "port" : 2121
}
```



### 2.DELETE deleteServer


Delete the server corresponding to the alias specified in the path


***Endpoint:***

```bash
Method: DELETE
Type: RAW
URL: {{base_url}}/servers/fil
```



### 3.GET getServer


Return the server corresponding to the alias specified in the path


***Endpoint:***

```bash
Method: GET
Type: 
URL: {{base_url}}/servers/local
```



### 4.GET getServers


Return all existing servers


***Endpoint:***

```bash
Method: GET
Type: 
URL: {{base_url}}/servers
```



### 5.PUT updateServer


Update the server corresponding to the alias specified in the path with the parameters contained in the body


***Endpoint:***

```bash
Method: PUT
Type: RAW
URL: {{base_url}}/servers/fil
```



***Body:***

```js        
{
    "alias" : "filupdated",
    "address" : "filupdated.ftp.com",
    "port" : 21
}
```



---


## FtpResource

Ftp resource (exposed at "{alias}" path) which allows anyone to access the ftp server registered on the flopBox platform and to perform operations specific to the ftp protocol

### Indices

  * [createRepository](#1-createrepository)
  * [deleteRepository](#2-deleterepository)
  * [getFile](#3-getfile)
  * [getList](#4-getlist)
  * [getRepository](#5-getrepository)
  * [renameFile](#6-renamefile)
  * [uploadFIle](#7-uploadfile)


--------


### 1.POST createRepository


Create a new repository at the given path


***Endpoint:***

```bash
Method: POST
Type: 
URL: {{base_url}}/{{server_alias}}/repositories/newRepo
```



### 2.DELETE deleteRepository


Delete the specified directory


***Endpoint:***

```bash
Method: DELETE
Type: 
URL: {{base_url}}/{{server_alias}}/repositories/Projects/diagrams
```



### 3.GET getFile


Download the file of the specified server( "pyftp" )


***Endpoint:***

```bash
Method: GET
Type: 
URL: {{base_url}}/{{server_alias}}/files/.auto-changelog
```



### 4.GET getList


List the content of the specified directory( "/" ) of the specified server( "pyftp" )


***Endpoint:***

```bash
Method: GET
Type: 
URL: {{base_url}}/{{server_alias}}/list/
```



### 5.GET  getRepository


Download the repository of the specified server( "pyftp" )


***Endpoint:***

```bash
Method: GET
Type: 
URL: {{base_url}}/{{server_alias}}/repositories/test
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| downloadFolder | /home/adrien/demo/ |  |
| mode | passive |  |  



### 6.PUT renameFile  


Rename the file/repository of the specified path of the specified server


***Endpoint:***

```bash
Method: PUT
Type: 
URL: {{base_url}}/{{server_alias}}/rename/fileTest
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| name | newName |  |



### 7.PUT uploadFIle


Upload the ?path=file/directory in the specified directory of the specified server


***Endpoint:***

```bash
Method: PUT
Type: 
URL: {{base_url}}/{{server_alias}}/test
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| localPath | /home/adrien/demo/test |  |
| mode | passive |  |



---
