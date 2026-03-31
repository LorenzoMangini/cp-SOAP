✅ Forma correta no Postman
Method
POST
URL
http://localhost:8080/hospital/agendamento
Headers
Key	Value
Content-Type	text/xml;charset=UTF-8
SOAPAction	"cadastrarPaciente"
Body
raw → XML
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:ser="http://service.hospital.com.br/">
   <soapenv:Header/>
   <soapenv:Body>
      <ser:cadastrarPaciente>
         <nome>Joao Silva</nome>
         <cpf>12345678910</cpf>
         <dataNascimento>1990-05-10</dataNascimento>
         <telefone>11999999999</telefone>
         <email>joao@email.com</email>
      </ser:cadastrarPaciente>
   </soapenv:Body>
</soapenv:Envelope>
❌ O que você fez (errado)
POST http://localhost:8080/hospital/agendamento?Content-Type=text/xml;charset=UTF-8&SOAPAction=nomeDaOperacao

Isso transforma os headers em query params, e o SOAP não reconhece.

✔️ Estrutura final
URL
http://localhost:8080/hospital/agendamento
Headers
Content-Type: text/xml;charset=UTF-8
SOAPAction: "cadastrarPaciente"
Body
raw → XML
🧪 Dica importante

Se ainda der erro:

teste também sem SOAPAction
Content-Type: text/xml;charset=UTF-8

Muitos serviços SOAP Spring Boot não exigem SOAPAction.
