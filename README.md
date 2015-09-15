Proposal: Secure Sharing using Group Association on Mobile Devices

Team Ninja
Douglas Choi
Harshal Mahangare

Motivation and Problem Statement

This app is an android secure local wifi peer-to-peer file transfer application. This app will be able to host a secure network peer group for file transfer, allow other devices to connect, and securely transfer files within the peer group. The app will perform initial authentication by requiring the connector to send an image of themselves so that the host will be able to confirm the group’s identity before accepting or rejecting the user. 
	
The app will provide three areas of security. The first is proper authorization of a user before the admission to a group. This involves protection against a man-in-the-middle attack, which involves a group outsider attacker to connect to a group, and an evil twin attack, which involves an attack to make the user connect to a malicious device. The second is data security from all users outside the group. An eavesdropper to any wireless data transfer in the group should not be able to read any meaningful data [1]. Lastly, the app will secure against tampering with keys for data encryption distributed to the members of a peer group. The keys should only be functional for the duration of the connection, and will otherwise be expired when the user leaves the session [2].

Proposed Solution / Approach

Wi-Fi peer to peer communication would be established between a new device (android device) wanting to join a group and an existing peer using group association rules. The group association or admission rules would comprise of:
1. A Short Authenticated Strings (SAS) cryptographic protocol such as Peer Verify Size Verify SAS [1]
2. Public/private key generation either using a Centralized Group Key Management [3] which will be implemented using a cryptographic library Bouncy Castle
3. An Out-Of-Band (OOB) channel [1] using Peer Verify Image (of new person using cellphone camera)
4. Group counting or member list to make sure there is a consistent count of members in the group with all peers. [1]

Together these would be used as the basis of establishing the group association for any new device/person. Once association is established all communication would be using SSL and sessions would also have key refreshing and expiration.

Details of Implementation

This application will be developed on the Android platform using Java and the Android SDK. We will also use OpenSSL for secure data transfer, and Java cryptography package (javax.crypto) for key generation. The application will require at least three or more Android devices with a camera for a group connection.

Timeline and Work Distribution

The implementation timeline is as follows:

DC - Douglas Choi
HM - Harshal Mahangare

Week 1
Project setup - DC, HM
Implementation of Wifi-P2P for Android devices to discover and connect to one another - DC, HM
Week 2
Implementation of file transfer through a P2P channel - DC, HM
Week 3
Implementation of using camera to send a self-portrait to host - DC
Add ability for host to accept or reject user - HM
Week 4 - 5
Implement group creation and association logic - DC, HM
Week 6
Add encryption to data transfer - DC, HM 
Week 7 - 9
Session key protection logic - DC, HM
Week 10
Testing - DC, HM


References:

[1] Groupthink: Usability of Secure Group Association for Wireless Devices. Rishab Nithyanand, Nitesh Saxena, Gene Tsudik, Ersin Uzun
[2] Security Concept for Peer-to-Peer Systems. Stefan Kraxberger, Udo Payer
[3] On the Performance of Group Key Agreement Protocols. Yair Amir, Yongdae Kim, Cristina Nita-Rotaru, Gene Tsudik
