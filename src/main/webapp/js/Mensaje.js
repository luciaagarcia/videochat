class Mensaje {
    constructor(texto, hora) {
        this.texto = texto;
        this.hora = hora ? hora : new Date(Date.now()).toLocaleString();
    }

}