Librizzi esercitazione TEP
Java DataBase management
Client:
	Classe utente, manda messaggi al server, la ricezione dei messaggi è gestita da receiver, che viene creato in questa classe
Receiver:
	stampa i messaggi inviati dal server all'utente
Server:
	classe Server, accetta una sola connessione, non avrebbe avuto senso diversamente visto che il database accetta una sola connessione alla volta
	Gestisce tutte le operazione dell'utente, fra cui login, signin, users, per stampare tutti i nomi degli utenti registrati, mails, per stampare tutte le mail registrate, e quit, per chiudere il socket
	Una volta loggato si riceve un messaggio, si ha solamente l'opzione di fare il logout e chiudere il socket (quit)
Crypt:
	Contiene tutte le funzione crittografiche (usato solo per l'hash visto che la comunicazione non è criptata)
in h2-1.4.200.jar ho dovuto aggiungere i .class di Receiver e Crypt, perché quando settavo il classpath a quello del database, non vedeva più le classi nella stessa directory, ho provato ad impostare più percorsi usando il separatore ":" ma comunque non funzionava, quindi dopo 2 giorni di tentativi ho deciso di adottare questa soluzione poco elegante, credo sia perché la jvm da priorità ai file jar nell'impostare il file path.