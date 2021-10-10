define(['knockout', 'appController', 'ojs/ojmodule-element-utils', 'accUtils'],
    function(ko, app, moduleUtils, accUtils) {

        function ChatViewModel() {
            var self = this;

            this.user = app.user;

            self.recipient = ko.observable();

            self.chat = ko.observable(new Chat(ko, this.user));

            self.videoChat = ko.observable(new VideoChat(ko));

            self.estadoChatDeTexto = self.chat().estado;
            self.estadoSignaling = self.videoChat().estado;
            self.errorChatDeTexto = self.chat().error;
            self.errorSignaling = self.videoChat().error;

            // Header Config
            self.headerConfig = ko.observable({ 'view': [], 'viewModel': null });
            moduleUtils.createView({ 'viewPath': 'views/header.html' }).then(function(view) {
                self.headerConfig({ 'view': view, 'viewModel': app.getHeaderModel() })
            })

            self.connected = function() {
                accUtils.announce('Chat page loaded.');
                document.title = "Chat";

                getUsuariosConectados();
            };

            function getUsuariosConectados() {
                var data = {
                    url: "users/getUsuariosConectados",
                    type: "get",

                    contentType: 'application/json',
                    success: function(response) {
                        for (var i = 0; i < response.length; i++) {
                            var user = {
                                userName: response[i].name,
                                picture: ko.observable(null)
                            };
                            var userName = response[i].name;
                            self.chat().addUsuario(userName);
                            self.getImagen(user);
                        }

                    },
                    error: function(response) {
                        self.error(response.responseJSON.error);
                    }
                };
                $.ajax(data);


            }

            self.getImagen = function(user) {

                var data = {
                    url: "users/getImagenesConectados/" + user.userName,
                    type: "get",
                    contentType: 'application/json',
                    success: function(response) {
                        var picture = response.picture;
                        user.picture(picture);
                    },
                    error: function(response) {
                        self.error(response.responseJSON.error);
                    }
                };
                $.ajax(data);

            }

            self.encenderVideoLocal = function() {
                self.videoChat().encenderVideoLocal();
            }

            self.crearConexion = function() {
                self.videoChat().crearConexion();
            }

            self.enviarOferta = function(destinatario) {
                self.videoChat().enviarOferta(destinatario.nombre);
            }

            self.disconnected = function() {
                self.chat().close();
            };

            self.transitionCompleted = function() {
                // Implement if needed
            };

        }

        return ChatViewModel;
    }
);