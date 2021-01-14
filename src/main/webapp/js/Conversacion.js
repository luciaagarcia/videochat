class Conversacion {
    constructor(ko, nombreInterlocutor, chat) {
        this.nombreInterlocutor = nombreInterlocutor;
        this.mensajes = ko.observableArray([]);
        this.textoAEnviar = ko.observable("");
        this.chat = chat;
        this.visible = ko.observable(true);
    }

    addMensaje(mensaje) {
        this.mensajes.push(mensaje);
    }

    enviar() {
        var mensaje = {
            type: "PARTICULAR",
            destinatario: this.nombreInterlocutor,
            texto: this.textoAEnviar()
        };
        this.chat.enviar(mensaje);
        var mensaje = new Mensaje(this.textoAEnviar());
        this.addMensaje(mensaje);
    }

    obtenerMensajes() {
        var mensajesObtenidos = this.chat.recuperarMensaje();
        console.log(mensajesObtenidos);
        console.log(mensajesObtenidos.length);
        for (var i = 0; i < mensajesObtenidos.length; i++) {
            this.mensajes.push(mensajesObtenidos[i]);
        }

        // mensajesObtenidos.forEach(mensaje => function() {

        //     this.mensajes.push(mensaje);
        // });
    }
}