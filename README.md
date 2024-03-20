Petite application toute simple pour envoyer sa position GPS (et quelques autres infos) à la FFVL grace à l'API mise à disposition:

https://data.ffvl.fr/api/?help=tracker

L'appli doit avoir accès à la position de l'appareil (logique)
J'ai commencé par vouloir utiliser un periodicworker https://developer.android.com/develop/background-work/background-tasks/persistent?hl=en, mais finalement, vu la contrainte de périodicité de 15minutes minimum, j'ai fini par opté pour un foregroundService https://developer.android.com/develop/background-work/services/foreground-services avec un wakelock pour ne pas se faire jetter lorsque ca tourne en arrière plan.
Si tu trouves que c'est moche, dis-moi comment faire mieux pour envoyer une position toutes les 60 secondes :)
