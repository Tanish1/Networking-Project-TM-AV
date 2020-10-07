BBCA (Better Basic Chat App)

A better verison of BCA (Better Chat App) with a lot of cooler features and controls. Could be used ANYWHERE in the world with your friends and family, as long as the server is running and the people who want to chat know the IP address of it. It is so simple and feature-rich that you won't even CONSIDER using iMessage, Discord or any other apps because of how amazing this app is. (No iOS application as of yet, but there are workarounds!)

First off, clone or download the repository, and to start using, ensure that you set your firewall settings so that the server can recieve or send signals to the internet. Make sure you start ChatServer.java and share your IP address (public IP address if the people you are chatting with aren't connected to your router, use the website to see it: https://whatismyipaddress.com/) Then, enter it in the ChatClient.java for the clients who will join the server, and you can start talking! Ensure that you input the port 54321, and if that isn't available, you can always change it in ChatServer.java.

When you enter all of that information, the ChatClient will request a name - it can be anything, the server doesn't perform background checks or confirm if you are real. The only caveat is you cannot use a name that another person already has, so if Person A has dog, then you cannot have the username dog if the person has already joined in before you, so it's first come first serve.

Now, you can start sending messages! All you have to do is type in the message and it will be sent to everyone (by everyone I mean the people logged into the server, and of course the CIA servers). When you type it in, you don't see anything, and don't be alarmed that your message didn't send, it surely did... If you aren't sure, check the server side, and if that didn't work, you're doing something wrong, which YOU have to fix, or your friends and family will be upset. If the message goes through, the client sees nothing, but the other clients and the server see the message. And, you can see all incoming messages, in the format of NAME:MESSAGE. If you wish to send a message to just one person, include an @, followed by the person's username, then the message.

If you see a comment that you would love to respond to, but are not exactly how to put into words, just use a reaction! Start by typing an asterisk (*) followed by the reaction you wish to use. Just make sure that any reactions that have multiple words are separated with dashes, not spaces, as that will cause an error.

Whenever you want to leave, you just have to type /quit, and you will be disconnected from the server, and everyone can see if you left. 

Side Note: IntelliJ wasn't working for Tanish, so he shared the files from Google Drive and then Alex posted them after making his revisions, after merging, everything stopped working on IntelliJ.
