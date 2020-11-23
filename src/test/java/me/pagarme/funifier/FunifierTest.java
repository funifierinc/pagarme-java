package me.pagarme.funifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import me.pagar.BankAccountType;
import me.pagar.model.Address;
import me.pagar.model.BankAccount;
import me.pagar.model.Billing;
import me.pagar.model.Card;
import me.pagar.model.Customer;
import me.pagar.model.Document;
import me.pagar.model.Item;
import me.pagar.model.PagarMe;
import me.pagar.model.Plan;
import me.pagar.model.Recipient;
import me.pagar.model.Recipient.TransferInterval;
import me.pagar.model.Transaction.PaymentMethod;
import me.pagar.model.Shipping;
import me.pagar.model.SplitRule;
import me.pagar.model.Subscription;
import me.pagar.model.Transaction;

public class FunifierTest {

	public static void main(String[] args) throws Exception {
		new FunifierTest();
	}
	
	public static String PAGARME_APIKEY_TEST = "ak_test_Bsb5eHtP8QleB5CHNIEhIdisQBmKko";
	public static String PAGARME_MARKETPLACE_RECIPIENT_ID = "re_ckhq954yw0chj0g9tij7oymer";
	
	public FunifierTest() throws Exception {

		PagarMe.init(PAGARME_APIKEY_TEST);
		
		//cartao do comprador
		/*
		Card card = new Card();
        card.setHolderName("Nome do Comprador");
        card.setNumber("4242424242424242");
        card.setExpiresAt("1125");
        card.setCvv("909");
        card = card.save();
        */
        Card card = new Card().find("card_ckhuooe5f0mtq0g9tfk7e52m8");

		//1. recuperar conta da funifier
		//Recipient funifier = getRecipientMarketplace();
		Recipient funifier = new Recipient().find(PAGARME_MARKETPLACE_RECIPIENT_ID);
		System.out.println("FUNIFIER=" + funifier.getId());
		
		//2. recuperar conta do vendedor (configuração do checkout do vendedor no funifier)
		//Recipient vendedor = getRecipientVendedor(); vendedor.save();
		Recipient vendedor = new Recipient().find("re_ckhrwdxox0f9u0g9t31l3juu1");
		System.out.println("VENDEDOR=" + vendedor.getId());

		//3. recupera conta do cliente (gerar com base nos dados cadastrais da conta fuinfier do comprador)
		Customer cliente = getCustomerComprador();
		
		/*
		//4. criar compra com cartão (compra direta)
		Transaction compra = getTransactionCartaoComSplit(10000, card, cliente, vendedor, funifier);
		compra.save();
		System.out.println("COMPRA=" + compra.getId());
		*/
		
		
		//5. criar plano (assinatura de preço fixo)
		//Plan plan = getPlan(); plan.save();
		Plan plano = new Plan().find("520489");
		System.out.println("PLANO=" + plano.getId());
		
		//6. criar assinatura com cartão (assinatura de plano)
		//Subscription assinatura = getAssinaturaCartaoComSplit(plano, card, cliente, vendedor, funifier);
		//assinatura = assinatura.save();
		
		//7. localizar assinatura
		Subscription assinatura = new Subscription().find("539136");
		System.out.println("ASSINATURA=" + assinatura.getId());
		
		//8. cancelar assinatura
		assinatura.cancel();
		System.out.println("CANCELANDO ASSINATURA=" + assinatura.getId());
		
		//9. assinatura por volume
		//10. coupon de desconto
		//11. outras moedas
	}

	public Plan getPlan() {
		Plan plan = new Plan();
        plan.setAmount(100);
        plan.setDays(30);
        plan.setName("Assinatura Teste");
        plan.setPaymentMethods(Arrays.asList(PaymentMethod.CREDIT_CARD));
        plan.setCharges(100);
        plan.setColor("#bababa");
        plan.setInstallments(2);
        plan.setTrialDays(3);
        plan.setInvoiceReminder(3);
        return plan;
	}
	
	public Subscription getAssinaturaCartaoComSplit(Plan plan, Card card, Customer cliente, Recipient vendedor, Recipient funifier) {
		Subscription subscription = new Subscription();
        subscription.setCreditCardSubscriptionWithCardId(plan.getId(), card.getId(), cliente);
        subscription.setSoftDescriptor("boatrace");

        Collection<SplitRule> splitRules = getSplitRules(vendedor, funifier);
        subscription.setSplitRules(splitRules);
        
        Map<String, Object> metadata =  new HashMap<String, Object>();
        metadata.put("buyer_funifier_account_id", "123456");
        subscription.setMetadata(metadata);
        
        return subscription;
	}
	
	public Collection<SplitRule> getSplitRules(Recipient vendedor, Recipient funifier) {
		Collection<SplitRule> splitRules = new ArrayList<SplitRule>();
        
        //vendedor = getRecipientVendedor();
        SplitRule splitRule = new SplitRule();
        splitRule.setRecipientId(vendedor.getId());
        splitRule.setPercentage(85);
        splitRule.setLiable(true);
        splitRule.setChargeProcessingFee(true);
        splitRules.add(splitRule);

        //funifier  = getRecipientMarketplace();
        SplitRule splitRule2 = new SplitRule();
        splitRule2.setRecipientId(funifier.getId());
        splitRule2.setPercentage(15);
        splitRule2.setLiable(false);
        splitRule2.setChargeProcessingFee(false);

        splitRules.add(splitRule2);
        return splitRules;
	}
	
	@SuppressWarnings("unchecked")
	public Transaction getTransactionCartaoComSplit(Integer amount, Card card, Customer cliente, Recipient vendedor, Recipient funifier) {
		Transaction transaction = new Transaction();
		
	    Billing billing = new Billing(); 
	    billing.setName("Phineas Flynn");
	    Address address  = new Address(); 
	    address.setCity("Santo Andre");
	    address.setCountry("br");
	    address.setState("sp");
	    address.setNeighborhood("Parque Miami");
	    address.setStreet("Rua Rio Jari");
	    address.setZipcode("09133180");
	    address.setStreetNumber("7");
	    billing.setAddress(address);

	    Shipping shipping = new Shipping();
	    shipping.setAddress(address);
	    shipping.setName("Phineas Flynn");
	    shipping.setFee(3200);

	    Collection<Item> items = new ArrayList();
		Item item = new Item(); 
		item.setId("OX890");//id do pacote do marketplace
		item.setQuantity(10);//quantidade
		item.setUnitPrice(120);//valor por unidade
		item.setTangible(Boolean.TRUE);
		item.setTitle("Sailboat Race Gamification Interface Package");
		
		transaction.setCustomer(cliente);
	    transaction.setShipping(shipping);
	    transaction.setBilling(billing);
	    transaction.setItems(items);
	    
        transaction.setCapture(true);
        transaction.setAmount(amount);
        transaction.setPaymentMethod(Transaction.PaymentMethod.CREDIT_CARD);
        transaction.setCardHolderName(card.getHolderName());
        transaction.setCardExpirationDate(card.getExpiresAt());
        transaction.setCardCvv((card.getCvv()));
        transaction.setCardNumber(card.getNumber());

        Collection<SplitRule> splitRules = getSplitRules(vendedor, funifier);
        transaction.setSplitRules(splitRules);
        
        return transaction;
	}
	
	public Recipient getRecipientMarketplace() {
		BankAccount account = new BankAccount();
		account.setAgencia("2901");
		account.setAgenciaDv("0");
		account.setConta("243370");
		account.setContaDv("2");
		account.setBankCode("001");
		account.setType(BankAccountType.CORRENTE);
		account.setDocumentNumber("69549486168");//Documento identificador do titular da conta (cpf ou cnpj) Ex: 35146484252
		account.setLegalName("Funifier Marketplace");//Nome completo (se pessoa física) ou razão social (se pessoa jurídica). Até 30 caractéres
		//account.setId(123456);
		
		Recipient recipient = new Recipient();
		//recipient.setId("re_funifier_account_id");
		recipient.setId("re_ckhq954yw0chj0g9tij7oymer");
		recipient.setBankAccount(account);
		recipient.setAnticipatableVolumePercentage(85);//Porcentagem do valor passível de antecipação para este recebedor.
		recipient.setAutomaticAnticipationEnabled(true);//Se o recebedor está habilitado para receber automaticamente ou não o valor disponível para antecipação.
		recipient.setTransferDay(5);//Dia no qual o recebedor vai ser pago. Depende do transfer_interval. Se for daily, não é necessário. Se for weekly pode ser de 1 (segunda) a 5 (sexta). Se for monthly, pode ser de 1 a 31.
		recipient.setTransferEnabled(true);//Variável que indica se o recebedor pode receber os pagamentos automaticamente
		recipient.setTransferInterval(TransferInterval.WEEKLY);
		return recipient;
	}
	public Recipient getRecipientVendedor() {
		BankAccount account = new BankAccount();
		account.setAgencia("2901");
		account.setAgenciaDv("0");
		account.setConta("243370");
		account.setContaDv("2");
		account.setBankCode("001");
		account.setType(BankAccountType.CORRENTE);
		account.setDocumentNumber("69549486168");//Documento identificador do titular da conta (cpf ou cnpj) Ex: 35146484252
		account.setLegalName("Nome do Vendedor");//Nome completo (se pessoa física) ou razão social (se pessoa jurídica). Até 30 caractéres
		//account.setId(123456);
		
		Recipient recipient = new Recipient();
		//recipient.setId("re_funifier_account_id");
		recipient.setId("re_vendedor_account_id");
		recipient.setBankAccount(account);
		recipient.setAnticipatableVolumePercentage(85);//Porcentagem do valor passível de antecipação para este recebedor.
		recipient.setAutomaticAnticipationEnabled(true);//Se o recebedor está habilitado para receber automaticamente ou não o valor disponível para antecipação.
		recipient.setTransferDay(5);//Dia no qual o recebedor vai ser pago. Depende do transfer_interval. Se for daily, não é necessário. Se for weekly pode ser de 1 (segunda) a 5 (sexta). Se for monthly, pode ser de 1 a 31.
		recipient.setTransferEnabled(true);//Variável que indica se o recebedor pode receber os pagamentos automaticamente
		recipient.setTransferInterval(TransferInterval.WEEKLY);
		return recipient;
	}
	@SuppressWarnings("unchecked")
	public Customer getCustomerComprador() {
		Customer customer = new Customer();
	    customer.setType(Customer.Type.INDIVIDUAL);
	    customer.setExternalId("1001");
	    customer.setName("Phineas Flynn");
	    customer.setBirthday("1999-07-09");
	    customer.setEmail("phineas@threestatearea.com");
	    customer.setCountry("br");

	    Collection<Document> documents = new ArrayList();
	    Document document = new Document();
	    document.setType(Document.Type.CPF);
	    document.setNumber("77551442758");
	    documents.add(document);
	    customer.setDocuments(documents);

	    customer.setDocumentNumber("69549486168");

	    Collection<String> phones = new ArrayList();
	    phones.add("+5511982657575");
	    customer.setPhoneNumbers(phones);
	    return customer;
	}
}
