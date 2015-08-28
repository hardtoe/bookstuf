package com.bookstuf.appengine;

import com.google.inject.Singleton;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.ApplicationFee;
import com.stripe.model.ApplicationFeeCollection;
import com.stripe.model.Balance;
import com.stripe.model.BalanceTransaction;
import com.stripe.model.BalanceTransactionCollection;
import com.stripe.model.BankAccount;
import com.stripe.model.Card;
import com.stripe.model.Charge;
import com.stripe.model.ChargeCollection;
import com.stripe.model.ChargeRefundCollection;
import com.stripe.model.Coupon;
import com.stripe.model.CouponCollection;
import com.stripe.model.Customer;
import com.stripe.model.CustomerCollection;
import com.stripe.model.CustomerSubscriptionCollection;
import com.stripe.model.DeletedAccount;
import com.stripe.model.DeletedBankAccount;
import com.stripe.model.DeletedCard;
import com.stripe.model.DeletedCoupon;
import com.stripe.model.DeletedCustomer;
import com.stripe.model.DeletedInvoiceItem;
import com.stripe.model.DeletedPlan;
import com.stripe.model.Dispute;
import com.stripe.model.DisputeCollection;
import com.stripe.model.Event;
import com.stripe.model.EventCollection;
import com.stripe.model.ExternalAccount;
import com.stripe.model.ExternalAccountCollection;
import com.stripe.model.FeeRefund;
import com.stripe.model.FeeRefundCollection;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;
import com.stripe.model.InvoiceItem;
import com.stripe.model.InvoiceItemCollection;
import com.stripe.model.InvoiceLineItemCollection;
import com.stripe.model.Plan;
import com.stripe.model.PlanCollection;
import com.stripe.model.Refund;
import com.stripe.model.Reversal;
import com.stripe.model.Subscription;
import com.stripe.model.Transfer;
import com.stripe.model.TransferCollection;
import com.stripe.model.TransferReversalCollection;

@Singleton
public class StripeApi extends StripeApiBase {
	private final ChargeApi chargeApi = new ChargeApi();

	/**
	 * <h1>Charges</h1>
	 * <p>
	 * To charge a credit or a debit card, you create a charge object. You can
	 * retrieve and refund individual charges as well as list all charges.
	 * Charges are identified by a unique random ID.
	 * </p>
	 */
	public final ChargeApi charge() {
		return chargeApi;
	}

	public final class ChargeApi {
		/**
		 * <h1>Create a charge</h1>
		 * <p>
		 * To charge a credit card, you create a charge object. If your API key
		 * is in test mode, the supplied payment source (e.g., card or Bitcoin
		 * receiver) won't actually be charged, though everything else will
		 * occur as if in live mode. (Stripe assumes that the charge would have
		 * completed successfully).
		 * </p>
		 * 
		 * @param amount
		 *            A positive integer in the <a href=
		 *            "https://support.stripe.com/questions/which-zero-decimal-currencies-does-stripe-support"
		 *            >smallest currency unit</a> (e.g 100
		 *            <strong>cents</strong> to charge $1.00, or 1 to charge ¥1,
		 *            a <a href=
		 *            "https://support.stripe.com/questions/which-zero-decimal-currencies-does-stripe-support"
		 *            >0-decimal currency</a>) representing how much to charge
		 *            the card. The minimum amount is $0.50 (or equivalent in
		 *            charge currency).
		 * 
		 * @param currency
		 *            3-letter <a href=
		 *            "https://support.stripe.com/questions/which-currencies-does-stripe-support"
		 *            >ISO code</a> for currency.
		 * 
		 */
		public final CreateMethod create(final int amount,
				final String currency) {
			return new CreateMethod().set("amount", amount).set("currency",
					currency);
		}

		public final class CreateMethod extends Method<Charge, CreateMethod> {

			@Override
			public final Charge get() throws StripeException {
				return Charge.create(params(), options());
			}

			/**
			 * The ID of an existing customer that will be charged in this
			 * request.
			 */
			public final CreateMethod customer(final String customer) {
				return set("customer", customer);
			}

			/**
			 * A payment source to be charged, such as a credit card. If you
			 * also pass a customer ID, the source must be the ID of a source
			 * belonging to the customer. Otherwise, if you do not pass a
			 * customer ID, the source you provide must either be a token, like
			 * the ones returned by <a
			 * href="https://stripe.com/docs/stripe.js">Stripe.js</a>, or a
			 * <span class="lang lang-java">Map</span> containing a user's
			 * credit card details, with the options described below. Although
			 * not all information is required, the extra info helps prevent
			 * fraud.
			 */
			public final CreateMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * An arbitrary string which you can attach to a charge object. It
			 * is displayed when in the web interface alongside the charge. Note
			 * that if you use Stripe to send automatic email receipts to your
			 * customers, your receipt emails will include the
			 * <code>description</code> of the charge(s) that they are
			 * describing.
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Whether or not to immediately capture the charge. When false, the
			 * charge issues an authorization (or pre-authorization), and will
			 * need to be <a href="#capture_charge">captured</a> later.
			 * Uncaptured charges expire in <strong>7 days</strong>. For more
			 * information, see <a href=
			 * "https://support.stripe.com/questions/can-i-authorize-a-charge-and-then-wait-to-settle-it-later"
			 * >authorizing charges and settling later</a>.
			 */
			public final CreateMethod capture(final String capture) {
				return set("capture", capture);
			}

			/**
			 * An arbitrary string to be displayed on your customer's credit
			 * card statement. This may be up to <strong>22 characters</strong>.
			 * As an example, if your website is <code>RunClub</code> and the
			 * item you're charging for is a race ticket, you may want to
			 * specify a <code>statement_descriptor</code> of
			 * <code>RunClub 5K race ticket</code>. The statement description
			 * may not include <code>&lt;&gt;"'</code> characters, and will
			 * appear on your customer's statement in capital letters. Non-ASCII
			 * characters are automatically stripped. While most banks display
			 * this information consistently, some may display it incorrectly or
			 * not at all.
			 */
			public final CreateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * The email address to send this charge's <a
			 * href="https://stripe.com/blog/email-receipts">receipt</a> to. The
			 * receipt will not be sent until the charge is paid. If this charge
			 * is for a customer, the email address specified here will override
			 * the customer's email address. Receipts will not be sent for test
			 * mode charges. If <code>receipt_email</code> is specified for a
			 * charge in live mode, a receipt will be sent regardless of your <a
			 * href="https://dashboard.stripe.com/account/emails">email
			 * settings</a>.
			 */
			public final CreateMethod receiptEmail(final String receiptEmail) {
				return set("receipt_email", receiptEmail);
			}

			/**
			 * An account to make the charge on behalf of. If specified, the
			 * charge will be attributed to the destination account for tax
			 * reporting, and the funds from the charge will be transferred to
			 * the destination account. The ID of the resulting transfer will be
			 * returned in the transfer field of the response. <a href=
			 * "/docs/connect/payments-fees#charging-through-the-platform">See
			 * the documentation</a> for details.
			 */
			public final CreateMethod destination(final String destination) {
				return set("destination", destination);
			}

			/**
			 * A fee in <strong>cents</strong> that will be applied to the
			 * charge and transferred to the application owner's Stripe account.
			 * To use an application fee, the request must be made on behalf of
			 * another account, using the Stripe-Account header, an OAuth key,
			 * or the <code>destination</code> parameter. For more information,
			 * see the application fees <a
			 * href="/docs/connect/collecting-fees">documentation</a>.
			 */
			public final CreateMethod applicationFee(final int applicationFee) {
				return set("application_fee", applicationFee);
			}

			/**
			 * Shipping information for the charge. Helps prevent fraud on
			 * charges for physical goods.
			 */
			public final CreateMethod shipping(final String shipping) {
				return set("shipping", shipping);
			}

			/**
			 * A set of key/value pairs that you can attach to a charge object.
			 * It can be useful for storing additional information about the
			 * customer in a structured format. It's often a good idea to store
			 * an email address in metadata for tracking later.
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a charge</h1>
		 * <p>
		 * Retrieves the details of a charge that has previously been created.
		 * Supply the unique charge ID that was returned from your previous
		 * request, and Stripe will return the corresponding charge information.
		 * The same information is returned when creating or refunding the
		 * charge.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the charge to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<Charge, RetrieveMethod> {

			@Override
			public final Charge get() throws StripeException {
				return Charge.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a charge</h1>
		 * <p>
		 * Updates the specified charge by setting the values of the parameters
		 * passed. Any parameters not provided will be left unchanged.
		 * </p>
		 * <p>
		 * This request accepts only the <code>description</code>,
		 * <code>metadata</code>, <code>receipt_email</code>and
		 * <code>fraud_details</code> as arguments.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Charge input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Charge, UpdateMethod> {
			private final Charge input;

			private UpdateMethod(final Charge input) {
				this.input = input;
			}

			@Override
			public final Charge get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * An arbitrary string which you can attach to a charge object. It
			 * is displayed when in the web interface alongside the charge. Note
			 * that if you use Stripe to send automatic email receipts to your
			 * customers, your receipt emails will include the
			 * <code>description</code> of the charge(s) that they are
			 * describing. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * This is the email address that the receipt for this charge will
			 * be sent to. If this field is updated, then a new email receipt
			 * will be sent to the updated address.
			 */
			public final UpdateMethod receiptEmail(final String receiptEmail) {
				return set("receipt_email", receiptEmail);
			}

			/**
			 * A set of key/value pairs you can attach to a charge giving
			 * information about its riskiness. If you believe a charge is
			 * fraudulent, include a <code>user_report</code> key with a value
			 * of <code>fraudulent</code>. If you believe a charge is safe,
			 * include a <code>user_report</code> key with a value of
			 * <code>safe</code>. Note that you must refund a charge before
			 * setting the <code>user_report</code> to <code>fraudulent</code>.
			 * Stripe will use the information you send to improve our fraud
			 * detection algorithms.
			 */
			public final UpdateMethod fraudDetails(final String fraudDetails) {
				return set("fraud_details", fraudDetails);
			}

			/**
			 * Shipping information for the charge. Helps prevent fraud on
			 * charges for physical goods.
			 */
			public final UpdateMethod shipping(final String shipping) {
				return set("shipping", shipping);
			}

			/**
			 * A set of key/value pairs that you can attach to a charge object.
			 * It can be useful for storing additional information about the
			 * charge in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Capture a charge</h1>
		 * <p>
		 * Capture the payment of an existing, uncaptured, charge. This is the
		 * second half of the two-step payment flow, where first you <a
		 * href="#create_charge">created a charge</a> with the capture option
		 * set to false.
		 * </p>
		 * <p>
		 * Uncaptured payments expire exactly seven days after they are created.
		 * If they are not captured by that point in time, they will be marked
		 * as refunded and will no longer be capturable.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final CaptureMethod capture(final Charge input) {
			return new CaptureMethod(input);
		}

		public final class CaptureMethod extends Method<Charge, CaptureMethod> {
			private final Charge input;

			private CaptureMethod(final Charge input) {
				this.input = input;
			}

			@Override
			public final Charge get() throws StripeException {
				return input.capture(params(), options());
			}

			/**
			 * The amount to capture, which must be less than or equal to the
			 * original amount. Any additional amount will be automatically
			 * refunded.
			 */
			public final CaptureMethod amount(final String amount) {
				return set("amount", amount);
			}

			/**
			 * An application fee to add on to this charge. Can only be used
			 * with Stripe Connect.
			 */
			public final CaptureMethod applicationFee(
					final String applicationFee) {
				return set("application_fee", applicationFee);
			}

			/**
			 * The email address to send this charge’s receipt to. This will
			 * override the previously-specified email address for this charge,
			 * if one was set. Receipts will not be sent in test mode.
			 */
			public final CaptureMethod receiptEmail(final String receiptEmail) {
				return set("receipt_email", receiptEmail);
			}

			/**
			 * An arbitrary string to be displayed on your customer’s credit
			 * card statement. This may be up to <em>22 characters</em>. As an
			 * example, if your website is <code>RunClub</code> and the item
			 * you’re charging for is a race ticket, you may want to specify a
			 * <code>statement_descriptor</code> of
			 * <code>RunClub 5K race ticket</code>. The statement description
			 * may not include <code>&lt;&gt;"'</code> characters, and will
			 * appear on your customer’s statement in capital letters. Non-ASCII
			 * characters are automatically stripped. Updating this value will
			 * overwrite the previous statement descriptor of this charge. While
			 * most banks display this information consistently, some may
			 * display it incorrectly or not at all.
			 */
			public final CaptureMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

		} // CaptureMethod

		/**
		 * <h1>List all charges</h1>
		 * <p>
		 * Returns a list of charges you've previously created. The charges are
		 * returned in sorted order, with the most recent charges appearing
		 * first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<ChargeCollection, AllMethod> {

			@Override
			public final ChargeCollection get() throws StripeException {
				return Charge.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * Only return charges for the customer specified by this customer
			 * ID.
			 */
			public final AllMethod customer(final String customer) {
				return set("customer", customer);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // ChargeApi

	private final RefundApi refundApi = new RefundApi();

	/**
	 * <h1>Refunds</h1>
	 * <p>
	 * Refund objects allow you to refund a charge that has previously been
	 * created but not yet refunded. Funds will be refunded to the credit or
	 * debit card that was originally charged. The fees you were originally
	 * charged are also refunded.
	 * </p>
	 */
	public final RefundApi refund() {
		return refundApi;
	}

	public final class RefundApi {
		/**
		 * <h1>Create a refund</h1>
		 * <p>
		 * When you create a new refund, you must specify a charge to create it
		 * on.
		 * </p>
		 * <p>
		 * Creating a new refund will refund a charge that has previously been
		 * created but not yet refunded. Funds will be refunded to the credit or
		 * debit card that was originally charged. The fees you were originally
		 * charged are also refunded.
		 * </p>
		 * <p>
		 * You can optionally refund only part of a charge. You can do so as
		 * many times as you wish until the entire charge has been refunded.
		 * </p>
		 * <p>
		 * Once entirely refunded, a charge can't be refunded again. This method
		 * will <span class="lang lang-java">throw</span> an error when called
		 * on an already-refunded charge, or when trying to refund more money
		 * than is left on a charge.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final CreateMethod create(final Charge input) {
			return new CreateMethod(input);
		}

		public final class CreateMethod extends Method<Refund, CreateMethod> {
			private final Charge input;

			private CreateMethod(final Charge input) {
				this.input = input;
			}

			@Override
			public final Refund get() throws StripeException {
				return input.getRefunds().create(params(), options());
			}

			/**
			 * A positive integer in <strong>cents</strong> representing how
			 * much of this charge to refund. Can only refund up to the
			 * unrefunded amount remaining of the charge.
			 */
			public final CreateMethod amount(final String amount) {
				return set("amount", amount);
			}

			/**
			 * Boolean indicating whether the application fee should be refunded
			 * when refunding this charge. If a full charge refund is given, the
			 * full application fee will be refunded. Else, the application fee
			 * will be refunded with an amount proportional to the amount of the
			 * charge refunded.
			 */
			public final CreateMethod refundApplicationFee(
					final boolean refundApplicationFee) {
				return set("refund_application_fee", refundApplicationFee);
			}

			/**
			 * Boolean indicating whether the transfer should be reversed when
			 * refunding this charge. The transfer will be reversed for the same
			 * amount being refunded (either the entire or partial amount).
			 */
			public final CreateMethod reverseTransfer(
					final boolean reverseTransfer) {
				return set("reverse_transfer", reverseTransfer);
			}

			/**
			 * String indicating the reason for the refund. If set, possible
			 * values are <code>duplicate</code>, <code>fraudulent</code>, and
			 * <code>requested_by_customer</code>. Specifying
			 * <code>fraudulent</code> as the reason when you believe the charge
			 * to be fraudulent will help us improve our fraud detection
			 * algorithms.
			 */
			public final CreateMethod reason(final String reason) {
				return set("reason", reason);
			}

			/**
			 * A set of key/value pairs that you can attach to a refund object.
			 * It can be useful for storing additional information about the
			 * refund in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a refund</h1>
		 * <p>
		 * By default, you can see the 10 most recent refunds stored directly on
		 * the charge object, but you can also retrieve details about a specific
		 * refund stored on the charge.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 *            ID of refund to retrieve.
		 * 
		 * @param charge
		 *            ID of the charge refunded.
		 * 
		 */
		public final RetrieveMethod retrieve(final Charge input,
				final String id, final String charge) {
			return new RetrieveMethod(input).set("id", id)
					.set("charge", charge);
		}

		public final class RetrieveMethod extends
				Method<Refund, RetrieveMethod> {
			private final Charge input;

			private RetrieveMethod(final Charge input) {
				this.input = input;
			}

			@Override
			public final Refund get() throws StripeException {
				return input.getRefunds().retrieve((String) params().get("id"),
						options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a refund</h1>
		 * <p>
		 * Updates the specified refund by setting the values of the parameters
		 * passed. Any parameters not provided will be left unchanged.
		 * </p>
		 * <p>
		 * This request only accepts metadata as an argument.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Refund input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Refund, UpdateMethod> {
			private final Refund input;

			private UpdateMethod(final Refund input) {
				this.input = input;
			}

			@Override
			public final Refund get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * A set of key/value pairs that you can attach to a refund object.
			 * It can be useful for storing additional information about the
			 * refund in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>List all refunds</h1>
		 * <p>
		 * You can see a list of the refunds belonging to a specific charge.
		 * Note that the 10 most recent refunds are always available by default
		 * on the charge object. If you need more than those 10, you can use
		 * this API method and the <code>limit</code> and
		 * <code>starting_after</code> parameters to page through additional
		 * refunds.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final Charge input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<ChargeRefundCollection, AllMethod> {
			private final Charge input;

			private AllMethod(final Charge input) {
				this.input = input;
			}

			@Override
			public final ChargeRefundCollection get() throws StripeException {
				return input.getRefunds().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // RefundApi

	private final CustomerApi customerApi = new CustomerApi();

	/**
	 * <h1>Customers</h1>
	 * <p>
	 * Customer objects allow you to perform recurring charges and track
	 * multiple charges that are associated with the same customer. The API
	 * allows you to create, delete, and update your customers. You can retrieve
	 * individual customers as well as a list of all your customers.
	 * </p>
	 */
	public final CustomerApi customer() {
		return customerApi;
	}

	public final class CustomerApi {
		/**
		 * <h1>Create a customer</h1>
		 * <p>
		 * Creates a new customer object.
		 * </p>
		 * 
		 */
		public final CreateMethod create() {
			return new CreateMethod();
		}

		public final class CreateMethod extends Method<Customer, CreateMethod> {

			@Override
			public final Customer get() throws StripeException {
				return Customer.create(params(), options());
			}

			/**
			 * An integer amount in cents that is the starting account balance
			 * for your customer. A negative amount represents a credit that
			 * will be used before attempting any charges to the customer’s
			 * card; a positive amount will be added to the next invoice.
			 */
			public final CreateMethod accountBalance(final String accountBalance) {
				return set("account_balance", accountBalance);
			}

			/**
			 * If you provide a coupon code, the customer will have a discount
			 * applied on all recurring charges. Charges you create through the
			 * API will not have the discount.
			 */
			public final CreateMethod coupon(final String coupon) {
				return set("coupon", coupon);
			}

			/**
			 * An arbitrary string that you can attach to a customer object. It
			 * is displayed alongside the customer in the dashboard. <span
			 * class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Customer’s email address. It’s displayed alongside the customer
			 * in your dashboard and can be useful for searching and tracking.
			 * <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final CreateMethod email(final String email) {
				return set("email", email);
			}

			/**
			 * The identifier of the plan to subscribe the customer to. If
			 * provided, the returned customer object will have a list of
			 * subscriptions that the customer is currently subscribed to. If
			 * you subscribe a customer to a plan without a free trial, the
			 * customer must have a valid card as well.
			 */
			public final CreateMethod plan(final String plan) {
				return set("plan", plan);
			}

			/**
			 * The quantity you’d like to apply to the subscription you’re
			 * creating (if you pass in a <code>plan</code>). For example, if
			 * your plan is 10 cents/user/month, and your customer has 5 users,
			 * you could pass 5 as the quantity to have the customer charged 50
			 * cents (5 x 10 cents) monthly. Defaults to 1 if not set. Only
			 * applies when the <code>plan</code> parameter is also provided.
			 */
			public final CreateMethod quantity(final String quantity) {
				return set("quantity", quantity);
			}

			/**
			 * The source can either be a token, like the ones returned by our
			 * <a href="/docs/stripe.js">Stripe.js</a>, or a dictionary
			 * containing a user’s credit card details (with the options shown
			 * below).
			 */
			public final CreateMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * A positive decimal (with at most two decimal places) between 1
			 * and 100. This represents the percentage of the subscription
			 * invoice subtotal that will be calculated and added as tax to the
			 * final amount each billing period. For example, a plan which
			 * charges $10/month with a <code>tax_percent</code> of 20.0 will
			 * charge $12 per invoice.
			 */
			public final CreateMethod taxPercent(final String taxPercent) {
				return set("tax_percent", taxPercent);
			}

			/**
			 * Unix timestamp representing the end of the trial period the
			 * customer will get before being charged. If set, trial_end will
			 * override the default trial period of the plan the customer is
			 * being subscribed to. The special value <code>now</code> can be
			 * provided to end the customer’s trial immediately. Only applies
			 * when the <code>plan</code> parameter is also provided.
			 */
			public final CreateMethod trialEnd(final String trialEnd) {
				return set("trial_end", trialEnd);
			}

			/**
			 * A set of key/value pairs that you can attach to a customer
			 * object. It can be useful for storing additional information about
			 * the customer in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a customer</h1>
		 * <p>
		 * Retrieves the details of an existing customer. You need only supply
		 * the unique customer identifier that was returned upon customer
		 * creation.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the customer to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<Customer, RetrieveMethod> {

			@Override
			public final Customer get() throws StripeException {
				return Customer
						.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a customer</h1>
		 * <p>
		 * Updates the specified customer by setting the values of the
		 * parameters passed. Any parameters not provided will be left
		 * unchanged. For example, if you pass the <strong>source</strong>
		 * parameter, that becomes the customer's active source (e.g., a card)
		 * to be used for all charges in the future. When you update a customer
		 * to a new valid source: for each of the customer's current
		 * subscriptions, if the subscription is in the <code>past_due</code>
		 * state, then the latest unpaid, unclosed invoice for the subscription
		 * will be retried (note that this retry will not count as an automatic
		 * retry, and will not affect the next regularly scheduled payment for
		 * the invoice). (Note also that no invoices pertaining to subscriptions
		 * in the <code>unpaid</code> state, or invoices pertaining to canceled
		 * subscriptions, will be retried as a result of updating the customer's
		 * source.)
		 * </p>
		 * <p>
		 * This request accepts mostly the same arguments as the customer
		 * creation call.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Customer input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Customer, UpdateMethod> {
			private final Customer input;

			private UpdateMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final Customer get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * An integer amount in cents that represents the account balance
			 * for your customer. Account balances only affect invoices. A
			 * negative amount represents a credit that decreases the amount due
			 * on an invoice; a positive amount increases the amount due on an
			 * invoice.
			 */
			public final UpdateMethod accountBalance(final String accountBalance) {
				return set("account_balance", accountBalance);
			}

			/**
			 * If you provide a coupon code, the customer will have a discount
			 * applied on all recurring charges. Charges you create through the
			 * API will not have the discount.
			 */
			public final UpdateMethod coupon(final String coupon) {
				return set("coupon", coupon);
			}

			/**
			 * ID of source to make the customer’s new default for invoice
			 * payments
			 */
			public final UpdateMethod defaultSource(final String defaultSource) {
				return set("default_source", defaultSource);
			}

			/**
			 * An arbitrary string that you can attach to a customer object. It
			 * is displayed alongside the customer in the dashboard. <span
			 * class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Customer’s email address. It’s displayed alongside the customer
			 * in your dashboard and can be useful for searching and tracking.
			 * <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod email(final String email) {
				return set("email", email);
			}

			/**
			 * The source can either be a token, like the ones returned by our
			 * <a href="/docs/stripe.js">Stripe.js</a>, or a dictionary
			 * containing a user’s credit card details (with the options shown
			 * below). Passing <code>source</code> will create a new source
			 * object, make it the new customer default source, and delete the
			 * old customer default if one exists. If you want to add additional
			 * sources instead of replacing the existing default, use the <a
			 * href="/docs/api#create_card">card creation API</a>. Whenever you
			 * attach a card to a customer, Stripe will automatically validate
			 * the card.
			 */
			public final UpdateMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * A set of key/value pairs that you can attach to a customer
			 * object. It can be useful for storing additional information about
			 * the customer in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete a customer</h1>
		 * <p>
		 * Permanently deletes a customer. It cannot be undone. Also immediately
		 * cancels any active subscriptions on the customer.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final Customer input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedCustomer, DeleteMethod> {
			private final Customer input;

			private DeleteMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final DeletedCustomer get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all customers</h1>
		 * <p>
		 * Returns a list of your customers. The customers are returned sorted
		 * by creation date, with the most recently created customers appearing
		 * first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<CustomerCollection, AllMethod> {

			@Override
			public final CustomerCollection get() throws StripeException {
				return Customer.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // CustomerApi

	private final CardApi cardApi = new CardApi();

	/**
	 * <h1>Cards</h1>
	 * <p>
	 * You can store multiple cards on a customer in order to charge the
	 * customer later. You can also store multiple debit cards on a recipient or
	 * a <a href="/docs/connect/managed-accounts">managed account</a> in order
	 * to transfer to those cards later.
	 * </p>
	 */
	public final CardApi card() {
		return cardApi;
	}

	public final class CardApi {
		/**
		 * <h1>Create a card</h1>
		 * <p>
		 * When you create a new credit card, you must specify a customer,
		 * recipient, or <a href="/docs/connect/managed-accounts">managed
		 * account</a> to create it on.
		 * </p>
		 * <p>
		 * If the card's owner has no default card, then the new card will
		 * become the default. However, if the owner already has a default then
		 * it will not change. To change the default, you should either <a
		 * href="/docs/api#update_customer">update the customer</a> to have a
		 * new <code>default_source</code>, <a
		 * href="/docs/api#update_recipient">update the recipient</a> to have a
		 * new <code>default_card</code>, or set
		 * <code>default_for_currency</code> to <code>true</code> when creating
		 * a card for a managed account.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param source
		 *            When adding a card to a customer, the parameter name is
		 *            <code>source</code>. When adding to an account, the
		 *            parameter name is <code>external_account</code>. The value
		 *            can either be a token, like the ones returned by our <a
		 *            href="/docs/stripe.js">Stripe.js</a>, or a dictionary
		 *            containing a user’s credit card details (with the options
		 *            shown below). Stripe will automatically validate the card.
		 * 
		 */
		public final CreateMethod create(final Customer input,
				final String source) {
			return new CreateMethod(input).set("source", source);
		}

		public final class CreateMethod extends Method<Card, CreateMethod> {
			private final Customer input;

			private CreateMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final Card get() throws StripeException {
				return input.createCard(params(), options());
			}

			/**
			 * Only applicable on accounts (not customers or recipients). If you
			 * set this to <code>true</code> (or if this is the first external
			 * account being added in this currency) this card will become the
			 * default external account for its currency.
			 */
			public final CreateMethod defaultForCurrency(
					final String defaultForCurrency) {
				return set("default_for_currency", defaultForCurrency);
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a card</h1>
		 * <p>
		 * You can always see the 10 most recent cards directly on a customer,
		 * recipient, or <a href="/docs/connect/managed-accounts">managed
		 * account</a>; this method lets you retrieve details about a specific
		 * card stored on the customer, recipient, or account.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 *            The ID of the card to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final Customer input,
				final String id) {
			return new RetrieveMethod(input).set("id", id);
		}

		public final class RetrieveMethod extends Method<Card, RetrieveMethod> {
			private final Customer input;

			private RetrieveMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final Card get() throws StripeException {
				return (Card) input.getSources().retrieve(
						(String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a card</h1>
		 * <p>
		 * If you need to update only some card details, like the billing
		 * address or expiration date, you can do so without having to re-enter
		 * the full card details. Stripe also works directly with card networks
		 * so that your customers can <a href=
		 * "https://support.stripe.com/questions/how-can-i-keep-customer-card-details-up-to-date"
		 * >continue using your service</a> without interruption.
		 * </p>
		 * <p>
		 * When you update a card, Stripe will automatically validate the card.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Card input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Card, UpdateMethod> {
			private final Card input;

			private UpdateMethod(final Card input) {
				this.input = input;
			}

			@Override
			public final Card get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * 
			 */
			public final UpdateMethod addressCity(final String addressCity) {
				return set("address_city", addressCity);
			}

			/**
			 * 
			 */
			public final UpdateMethod addressCountry(final String addressCountry) {
				return set("address_country", addressCountry);
			}

			/**
			 * 
			 */
			public final UpdateMethod addressLine1(final String addressLine1) {
				return set("address_line1", addressLine1);
			}

			/**
			 * 
			 */
			public final UpdateMethod addressLine2(final String addressLine2) {
				return set("address_line2", addressLine2);
			}

			/**
			 * 
			 */
			public final UpdateMethod addressState(final String addressState) {
				return set("address_state", addressState);
			}

			/**
			 * 
			 */
			public final UpdateMethod addressZip(final String addressZip) {
				return set("address_zip", addressZip);
			}

			/**
			 * Only applicable on accounts (not customers or recipients). If set
			 * to <code>true</code>, this card will become the default external
			 * account for its currency.
			 */
			public final UpdateMethod defaultForCurrency(
					final String defaultForCurrency) {
				return set("default_for_currency", defaultForCurrency);
			}

			/**
			 * 
			 */
			public final UpdateMethod expMonth(final String expMonth) {
				return set("exp_month", expMonth);
			}

			/**
			 * 
			 */
			public final UpdateMethod expYear(final String expYear) {
				return set("exp_year", expYear);
			}

			/**
			 * 
			 */
			public final UpdateMethod name(final String name) {
				return set("name", name);
			}

			/**
			 * 
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete a card</h1>
		 * <p>
		 * You can delete cards from a customer, recipient, or <a
		 * href="/docs/connect/managed-accounts">managed account</a>.
		 * </p>
		 * <p>
		 * For customers: if you delete a card that is currently the default
		 * source, then the most recently added source will become the new
		 * default. If you delete a card that is the last remaining source on
		 * the customer then the <code>default_source</code> attribute will
		 * become null.
		 * </p>
		 * <p>
		 * For recipients: if you delete the default card, then the most
		 * recently added card will become the new default. If you delete the
		 * last remaining card on a recipient, then the
		 * <code>default_card</code> attribute will become null.
		 * </p>
		 * <p>
		 * For accounts: if a card's <code>default_for_currency</code> property
		 * is true, it can only be deleted if it is the only external account
		 * for its currency, and the currency is not the Stripe account's
		 * default currency. Otherwise, before deleting the card, you must set
		 * another external account to be the default for the currency.
		 * </p>
		 * <p>
		 * Note that for cards belonging to customers, you may want to prevent
		 * customers on paid subscriptions from deleting all cards on file so
		 * that there is at least one default card for the next invoice payment
		 * attempt.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final Card input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedCard, DeleteMethod> {
			private final Card input;

			private DeleteMethod(final Card input) {
				this.input = input;
			}

			@Override
			public final DeletedCard get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all cards</h1>
		 * <p>
		 * You can see a list of the cards belonging to a customer, recipient,
		 * or <a href="/docs/connect/managed-accounts">managed account</a>. Note
		 * that the 10 most recent sources are always available on the customer
		 * object, and the 10 most recent external accounts are available on the
		 * account object. If you need more than those 10, you can use this API
		 * method and the <code>limit</code> and <code>starting_after</code>
		 * parameters to page through additional cards.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final Customer input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<ExternalAccountCollection, AllMethod> {
			private final Customer input;

			private AllMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final ExternalAccountCollection get() throws StripeException {
				return input.getSources().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // CardApi

	private final SubscriptionApi subscriptionApi = new SubscriptionApi();

	/**
	 * <h1>Subscriptions</h1>
	 * <p>
	 * Subscriptions allow you to charge a customer's card on a recurring basis.
	 * A subscription ties a customer to a particular plan <a
	 * href="#create_plan">you've created</a>.
	 * </p>
	 */
	public final SubscriptionApi subscription() {
		return subscriptionApi;
	}

	public final class SubscriptionApi {
		/**
		 * <h1>Create a subscription</h1>
		 * <p>
		 * Creates a new subscription on an existing customer.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param plan
		 *            The identifier of the plan to subscribe the customer to.
		 * 
		 */
		public final CreateMethod create(final Customer input, final String plan) {
			return new CreateMethod(input).set("plan", plan);
		}

		public final class CreateMethod extends
				Method<Subscription, CreateMethod> {
			private final Customer input;

			private CreateMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final Subscription get() throws StripeException {
				return input.createSubscription(params(), options());
			}

			/**
			 * The code of the coupon to apply to this subscription. A coupon
			 * applied to a subscription will only affect invoices created for
			 * that particular subscription.
			 */
			public final CreateMethod coupon(final String coupon) {
				return set("coupon", coupon);
			}

			/**
			 * Unix timestamp representing the end of the trial period the
			 * customer will get before being charged for the first time. If
			 * set, trial_end will override the default trial period of the plan
			 * the customer is being subscribed to. The special value
			 * <code>now</code> can be provided to end the customer's trial
			 * immediately.
			 */
			public final CreateMethod trialEnd(final String trialEnd) {
				return set("trial_end", trialEnd);
			}

			/**
			 * The source can either be a token, like the ones returned by our
			 * <a href="https://stripe.com/docs/stripe.js">Stripe.js</a>, or a
			 * <span class="lang lang-java">Map</span> containing a user's
			 * credit card details (with the options shown below). You must
			 * provide a source if the customer does not already have a valid
			 * source attached, and you are subscribing the customer for a plan
			 * that is not free. Passing <code>source</code> will create a new
			 * source object, make it the customer default source, and delete
			 * the old customer default if one exists. If you want to add an
			 * additional source to use with subscriptions, instead use the <a
			 * href="https://stripe.com/docs/api#create_card">card creation
			 * API</a> to add the card and then the <a
			 * href="https://stripe.com/docs/api#update customer">customer
			 * update API</a> to set it as the default. Whenever you attach a
			 * card to a customer, Stripe will automatically validate the card.
			 */
			public final CreateMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * The quantity you'd like to apply to the subscription you're
			 * creating. For example, if your plan is $10/user/month, and your
			 * customer has 5 users, you could pass 5 as the quantity to have
			 * the customer charged $50 (5 x $10) monthly. If you update a
			 * subscription but don't change the plan ID (e.g. changing only the
			 * trial_end), the subscription will inherit the old subscription's
			 * quantity attribute unless you pass a new quantity parameter. If
			 * you update a subscription and change the plan ID, the new
			 * subscription will not inherit the quantity attribute and will
			 * default to 1 unless you pass a quantity parameter.
			 */
			public final CreateMethod quantity(final String quantity) {
				return set("quantity", quantity);
			}

			/**
			 * A positive decimal (with at most two decimal places) between 1
			 * and 100. This represents the percentage of the subscription
			 * invoice subtotal that will be transferred to the application
			 * owner’s Stripe account. The request must be made with an OAuth
			 * key in order to set an application fee percentage. For more
			 * information, see the application fees <a
			 * href="/docs/connect/collecting-fees#subscriptions"
			 * >documentation</a>.
			 */
			public final CreateMethod applicationFeePercent(
					final String applicationFeePercent) {
				return set("application_fee_percent", applicationFeePercent);
			}

			/**
			 * A positive decimal (with at most two decimal places) between 1
			 * and 100. This represents the percentage of the subscription
			 * invoice subtotal that will be calculated and added as tax to the
			 * final amount each billing period. For example, a plan which
			 * charges $10/month with a <code>tax_percent</code> of 20.0 will
			 * charge $12 per invoice.
			 */
			public final CreateMethod taxPercent(final String taxPercent) {
				return set("tax_percent", taxPercent);
			}

			/**
			 * A set of key/value pairs that you can attach to a subscription
			 * object. It can be useful for storing additional information about
			 * the subscription in a structured format.
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a subscription</h1>
		 * <p>
		 * By default, you can see the 10 most recent active subscriptions
		 * stored on a customer directly on the customer object, but you can
		 * also retrieve details about a specific active subscription for a
		 * customer.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 *            ID of subscription to retrieve.
		 * 
		 * @param customer
		 * 
		 * 
		 */
		public final RetrieveMethod retrieve(final Customer input,
				final String id, final String customer) {
			return new RetrieveMethod(input).set("id", id).set("customer",
					customer);
		}

		public final class RetrieveMethod extends
				Method<Subscription, RetrieveMethod> {
			private final Customer input;

			private RetrieveMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final Subscription get() throws StripeException {
				return input.getSubscriptions().retrieve(
						(String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a subscription</h1>
		 * <p>
		 * Updates an existing subscription on a customer to match the specified
		 * parameters. When changing plans or quantities, we will optionally
		 * prorate the price we charge next month to make up for any price
		 * changes. To preview how the proration will be calculated, use the <a
		 * href="#retrieve_customer_invoice">upcoming invoice</a> endpoint.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Subscription input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends
				Method<Subscription, UpdateMethod> {
			private final Subscription input;

			private UpdateMethod(final Subscription input) {
				this.input = input;
			}

			@Override
			public final Subscription get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * The identifier of the plan to update the subscription to. If
			 * omitted, the subscription will not change plans.
			 */
			public final UpdateMethod plan(final String plan) {
				return set("plan", plan);
			}

			/**
			 * The code of the coupon to apply to the customer if you would like
			 * to apply it at the same time as updating the subscription.
			 */
			public final UpdateMethod coupon(final String coupon) {
				return set("coupon", coupon);
			}

			/**
			 * Flag telling us whether to prorate switching plans during a
			 * billing cycle.
			 */
			public final UpdateMethod prorate(final String prorate) {
				return set("prorate", prorate);
			}

			/**
			 * If set, the proration will be calculated as though the
			 * subscription was updated at the given time. This can be used to
			 * apply exactly the same proration that was previewed with <a
			 * href="#retrieve_customer_invoice">upcoming invoice</a> endpoint.
			 * It can also be used to implement custom proration logic, such as
			 * prorating by day instead of by second, by providing the time that
			 * you wish to use for proration calculations.
			 */
			public final UpdateMethod prorationDate(final String prorationDate) {
				return set("proration_date", prorationDate);
			}

			/**
			 * Unix timestamp representing the end of the trial period the
			 * customer will get before being charged for the first time. If
			 * set, trial_end will override the default trial period of the plan
			 * the customer is being subscribed to. The special value
			 * <code>now</code> can be provided to end the customer's trial
			 * immediately.
			 */
			public final UpdateMethod trialEnd(final String trialEnd) {
				return set("trial_end", trialEnd);
			}

			/**
			 * The source can either be a token, like the ones returned by our
			 * <a href="https://stripe.com/docs/stripe.js">Stripe.js</a>, or a
			 * <span class="lang lang-java">Map</span> containing a user's
			 * credit card details (with the options shown below). You must
			 * provide a source if the customer does not already have a valid
			 * source attached, and you are subscribing the customer for a plan
			 * that is not free. Passing <code>source</code> will create a new
			 * source object, make it the customer default source, and delete
			 * the old customer default if one exists. If you want to add an
			 * additional source to use with subscriptions, instead use the <a
			 * href="https://stripe.com/docs/api#create_card">card creation
			 * API</a> to add the card and then the <a
			 * href="https://stripe.com/docs/api#update customer">customer
			 * update API</a> to set it as the default. Whenever you attach a
			 * card to a customer, Stripe will automatically validate the card.
			 */
			public final UpdateMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * The quantity you'd like to apply to the subscription you're
			 * updating. For example, if your plan is $10/user/month, and your
			 * customer has 5 users, you could pass 5 as the quantity to have
			 * the customer charged $50 (5 x $10) monthly. If you update a
			 * subscription but don't change the plan ID (e.g. changing only the
			 * trial_end), the subscription will inherit the old subscription's
			 * quantity attribute unless you pass a new quantity parameter. If
			 * you update a subscription and change the plan ID, the new
			 * subscription will not inherit the quantity attribute and will
			 * default to 1 unless you pass a quantity parameter.
			 */
			public final UpdateMethod quantity(final String quantity) {
				return set("quantity", quantity);
			}

			/**
			 * A positive decimal (with at most two decimal places) between 1
			 * and 100 that represents the percentage of the subscription
			 * invoice amount due each billing period (including any bundled
			 * invoice items) that will be transferred to the application
			 * owner’s Stripe account. The request must be made with an OAuth
			 * key in order to set an application fee percentage . For more
			 * information, see the application fees <a
			 * href="/docs/connect/collecting-fees#subscriptions"
			 * >documentation</a>.
			 */
			public final UpdateMethod applicationFeePercent(
					final String applicationFeePercent) {
				return set("application_fee_percent", applicationFeePercent);
			}

			/**
			 * Update the amount of tax applied to this subscription. Changing
			 * the <code>tax_percent</code> of a subscription will only affect
			 * future invoices.
			 */
			public final UpdateMethod taxPercent(final String taxPercent) {
				return set("tax_percent", taxPercent);
			}

			/**
			 * A set of key/value pairs that you can attach to a subscription
			 * object. It can be useful for storing additional information about
			 * the subscription in a structured format.
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Cancel a subscription</h1>
		 * <p>
		 * Cancels a customer's subscription. If you set the
		 * <strong>at_period_end</strong> parameter to true, the subscription
		 * will remain active until the end of the period, at which point it
		 * will be canceled and not renewed. By default, the subscription is
		 * terminated immediately. In either case, the customer will not be
		 * charged again for the subscription. Note, however, that any pending
		 * invoice items that you've created will still be charged for at the
		 * end of the period unless manually <a
		 * href="#delete_invoiceitem">deleted</a>. If you've set the
		 * subscription to cancel at period end, any pending prorations will
		 * also be left in place and collected at the end of the period, but if
		 * the subscription is set to cancel immediately, pending prorations
		 * will be removed.
		 * </p>
		 * <p>
		 * By default, all unpaid invoices for the customer will be closed upon
		 * subscription cancellation. We do this in order to prevent unexpected
		 * payment retries once the customer has canceled a subscription.
		 * However, you can reopen the invoices manually after subscription
		 * cancellation to have us proceed with automatic retries, or you could
		 * even re-attempt payment yourself on all unpaid invoices before
		 * allowing the customer to cancel the subscription at all.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final CancelMethod cancel(final Subscription input) {
			return new CancelMethod(input);
		}

		public final class CancelMethod extends
				Method<Subscription, CancelMethod> {
			private final Subscription input;

			private CancelMethod(final Subscription input) {
				this.input = input;
			}

			@Override
			public final Subscription get() throws StripeException {
				return input.cancel(params(), options());
			}

			/**
			 * A flag that if set to true will delay the cancellation of the
			 * subscription until the end of the current period.
			 */
			public final CancelMethod atPeriodEnd(final String atPeriodEnd) {
				return set("at_period_end", atPeriodEnd);
			}

		} // CancelMethod

		/**
		 * <h1>List active subscriptions</h1>
		 * <p>
		 * You can see a list of the customer's active subscriptions. Note that
		 * the 10 most recent active subscriptions are always available by
		 * default on the customer object. If you need more than those 10, you
		 * can use the <code>limit</code> and <code>starting_after</code>
		 * parameters to page through additional subscriptions.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final Customer input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<CustomerSubscriptionCollection, AllMethod> {
			private final Customer input;

			private AllMethod(final Customer input) {
				this.input = input;
			}

			@Override
			public final CustomerSubscriptionCollection get()
					throws StripeException {
				return input.getSubscriptions().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // SubscriptionApi

	private final PlanApi planApi = new PlanApi();

	/**
	 * <h1>Plans</h1>
	 * <p>
	 * A subscription plan contains the pricing information for different
	 * products and feature levels on your site. For example, you might have a
	 * $10/month plan for basic features and a different $20/month plan for
	 * premium features.
	 * </p>
	 */
	public final PlanApi plan() {
		return planApi;
	}

	public final class PlanApi {
		/**
		 * <h1>Create a plan</h1>
		 * <p>
		 * You can create plans easily via the <a
		 * href="https://dashboard.stripe.com/plans">plan management</a> page of
		 * the Stripe dashboard. Plan creation is also accessible via the API if
		 * you need to create plans on the fly.
		 * </p>
		 * 
		 * @param id
		 *            Unique string of your choice that will be used to identify
		 *            this plan when subscribing a customer. This could be an
		 *            identifier like “gold” or a primary key from your own
		 *            database.
		 * 
		 * @param amount
		 *            A positive integer in <strong>cents</strong> (or 0 for a
		 *            free plan) representing how much to charge (on a recurring
		 *            basis).
		 * 
		 * @param currency
		 *            3-letter <a href=
		 *            "https://support.stripe.com/questions/which-currencies-does-stripe-support"
		 *            >ISO code for currency</a>.
		 * 
		 * @param interval
		 *            Specifies billing frequency. Either day, week, month or
		 *            year.
		 * 
		 * @param name
		 *            Name of the plan, to be displayed on invoices and in the
		 *            web interface.
		 * 
		 */
		public final CreateMethod create(final String id, final String amount,
				final String currency, final String interval, final String name) {
			return new CreateMethod().set("id", id).set("amount", amount)
					.set("currency", currency).set("interval", interval)
					.set("name", name);
		}

		public final class CreateMethod extends Method<Plan, CreateMethod> {

			@Override
			public final Plan get() throws StripeException {
				return Plan.create(params(), options());
			}

			/**
			 * The number of intervals between each subscription billing. For
			 * example, <code>interval=month</code> and
			 * <code>interval_count=3</code> bills every 3 months. Maximum of
			 * one year interval allowed (1 year, 12 months, or 52 weeks).
			 */
			public final CreateMethod intervalCount(final String intervalCount) {
				return set("interval_count", intervalCount);
			}

			/**
			 * An arbitrary string to be displayed on your customer’s credit
			 * card statement. This may be up to <strong>22 characters</strong>.
			 * As an example, if your website is <code>RunClub</code> and the
			 * item you’re charging for is your Silver Plan, you may want to
			 * specify a <code>statement_descriptor</code> of
			 * <code>RunClub Silver Plan</code>. The statement description may
			 * not include <code>&lt;&gt;"'</code> characters, and will appear
			 * on your customer’s statement in capital letters. Non-ASCII
			 * characters are automatically stripped. While most banks display
			 * this information consistently, some may display it incorrectly or
			 * not at all.
			 */
			public final CreateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * Specifies a trial period in (an integer number of) days. If you
			 * include a trial period, the customer won’t be billed for the
			 * first time until the trial period ends. If the customer cancels
			 * before the trial period is over, she’ll never be billed at all.
			 */
			public final CreateMethod trialPeriodDays(
					final String trialPeriodDays) {
				return set("trial_period_days", trialPeriodDays);
			}

			/**
			 * A set of key/value pairs that you can attach to a plan object. It
			 * can be useful for storing additional information about the plan
			 * in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a plan</h1>
		 * <p>
		 * Retrieves the plan with the given ID.
		 * </p>
		 * 
		 * @param id
		 *            The ID of the desired plan.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends Method<Plan, RetrieveMethod> {

			@Override
			public final Plan get() throws StripeException {
				return Plan.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a plan</h1>
		 * <p>
		 * Updates the name of a plan. Other plan details (price, interval,
		 * etc.) are, by design, not editable.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Plan input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Plan, UpdateMethod> {
			private final Plan input;

			private UpdateMethod(final Plan input) {
				this.input = input;
			}

			@Override
			public final Plan get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * Name of the plan, to be displayed on invoices and in the web
			 * interface.
			 */
			public final UpdateMethod name(final String name) {
				return set("name", name);
			}

			/**
			 * An arbitrary string to be displayed on your customer’s credit
			 * card statement. This may be up to <strong>22 characters</strong>.
			 * As an example, if your website is <code>RunClub</code> and the
			 * item you’re charging for is your Silver Plan, you may want to
			 * specify a <code>statement_descriptor</code> of
			 * <code>RunClub Silver Plan</code>. The statement description may
			 * not include <code>&lt;&gt;"'</code> characters, and will appear
			 * on your customer’s statement in capital letters. Non-ASCII
			 * characters are automatically stripped. While most banks display
			 * this information consistently, some may display it incorrectly or
			 * not at all. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * A set of key/value pairs that you can attach to a plan object. It
			 * can be useful for storing additional information about the plan
			 * in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete a plan</h1>
		 * <p>
		 * You can delete plans via the <a
		 * href="https://dashboard.stripe.com/plans">plan management</a> page of
		 * the Stripe dashboard. However, deleting a plan does not affect any
		 * current subscribers to the plan; it merely means that new subscribers
		 * can't be added to that plan. You can also delete plans via the API.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final Plan input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedPlan, DeleteMethod> {
			private final Plan input;

			private DeleteMethod(final Plan input) {
				this.input = input;
			}

			@Override
			public final DeletedPlan get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all plans</h1>
		 * <p>
		 * Returns a list of your plans.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends Method<PlanCollection, AllMethod> {

			@Override
			public final PlanCollection get() throws StripeException {
				return Plan.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // PlanApi

	private final CouponApi couponApi = new CouponApi();

	/**
	 * <h1>Coupons</h1>
	 * <p>
	 * A coupon contains information about a percent-off or amount-off discount
	 * you might want to apply to a customer. Coupons only apply to <a
	 * href="#invoices">invoices</a>; they do not apply to one-off <a
	 * href="#create_charge">charges</a>.
	 * </p>
	 */
	public final CouponApi coupon() {
		return couponApi;
	}

	public final class CouponApi {
		/**
		 * <h1>Create a coupon</h1>
		 * <p>
		 * You can create coupons easily via the <a
		 * href="https://dashboard.stripe.com/coupons">coupon management</a>
		 * page of the Stripe dashboard. Coupon creation is also accessible via
		 * the API if you need to create coupons on the fly.
		 * </p>
		 * <p>
		 * A coupon has either a <tt>percent_off</tt> or an <tt>amount_off</tt>
		 * and <tt>currency</tt>. If you set an <tt>amount_off</tt>, that amount
		 * will be subtracted from any invoice's subtotal. For example, an
		 * invoice with a subtotal of $10 will have a final total of $0 if a
		 * coupon with an <tt>amount_off</tt> of 2000 is applied to it and an
		 * invoice with a subtotal of $30 will have a final total of $10 if a
		 * coupon with an <tt>amount_off</tt> of 2000 is applied to it.
		 * </p>
		 * 
		 * @param duration
		 *            Specifies how long the discount will be in effect. Can be
		 *            <code>forever</code>, <code>once</code>, or
		 *            <code>repeating</code>.
		 * 
		 */
		public final CreateMethod create(final String duration) {
			return new CreateMethod().set("duration", duration);
		}

		public final class CreateMethod extends Method<Coupon, CreateMethod> {

			@Override
			public final Coupon get() throws StripeException {
				return Coupon.create(params(), options());
			}

			/**
			 * Unique string of your choice that will be used to identify this
			 * coupon when applying it a customer. This is often a specific code
			 * you’ll give to your customer to use when signing up (e.g.
			 * <em>FALL25OFF</em>). If you don’t want to specify a particular
			 * code, you can leave the ID blank and we’ll generate a random code
			 * for you.
			 */
			public final CreateMethod id(final String id) {
				return set("id", id);
			}

			/**
			 * A positive integer representing the amount to subtract from an
			 * invoice total (required if <code>percent_off</code> is not
			 * passed)
			 */
			public final CreateMethod amountOff(final String amountOff) {
				return set("amount_off", amountOff);
			}

			/**
			 * Currency of the <code>amount_off</code> parameter (required if
			 * <code>amount_off</code> is passed)
			 */
			public final CreateMethod currency(final String currency) {
				return set("currency", currency);
			}

			/**
			 * <em>required only if duration is repeating</em> If duration is
			 * repeating, a positive integer that specifies the number of months
			 * the discount will be in effect
			 */
			public final CreateMethod durationInMonths(
					final String durationInMonths) {
				return set("duration_in_months", durationInMonths);
			}

			/**
			 * A positive integer specifying the number of times the coupon can
			 * be redeemed before it’s no longer valid. For example, you might
			 * have a 50% off coupon that the first 20 readers of your blog can
			 * use.
			 */
			public final CreateMethod maxRedemptions(final String maxRedemptions) {
				return set("max_redemptions", maxRedemptions);
			}

			/**
			 * A positive integer between 1 and 100 that represents the discount
			 * the coupon will apply (required if <code>amount_off</code> is not
			 * passed)
			 */
			public final CreateMethod percentOff(final String percentOff) {
				return set("percent_off", percentOff);
			}

			/**
			 * Unix timestamp specifying the last time at which the coupon can
			 * be redeemed. After the redeem_by date, the coupon can no longer
			 * be applied to new customers.
			 */
			public final CreateMethod redeemBy(final String redeemBy) {
				return set("redeem_by", redeemBy);
			}

			/**
			 * A set of key/value pairs that you can attach to a coupon object.
			 * It can be useful for storing additional information about the
			 * coupon in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a coupon</h1>
		 * <p>
		 * Retrieves the coupon with the given ID.
		 * </p>
		 * 
		 * @param id
		 *            The ID of the desired coupon.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<Coupon, RetrieveMethod> {

			@Override
			public final Coupon get() throws StripeException {
				return Coupon.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a coupon</h1>
		 * <p>
		 * Updates the metadata of a coupon. Other coupon details (currency,
		 * duration, amount_off) are, by design, not editable.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Coupon input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Coupon, UpdateMethod> {
			private final Coupon input;

			private UpdateMethod(final Coupon input) {
				this.input = input;
			}

			@Override
			public final Coupon get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * A set of key/value pairs that you can attach to a coupon object.
			 * It can be useful for storing additional information about the
			 * coupon in a structured format.
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete a coupon</h1>
		 * <p>
		 * You can delete coupons via the <a
		 * href="https://dashboard.stripe.com/coupons">coupon management</a>
		 * page of the Stripe dashboard. However, deleting a coupon does not
		 * affect any customers who have already applied the coupon; it means
		 * that new customers can't redeem the coupon. You can also delete
		 * coupons via the API.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final Coupon input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedCoupon, DeleteMethod> {
			private final Coupon input;

			private DeleteMethod(final Coupon input) {
				this.input = input;
			}

			@Override
			public final DeletedCoupon get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all coupons</h1>
		 * <p>
		 * Returns a list of your coupons.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<CouponCollection, AllMethod> {

			@Override
			public final CouponCollection get() throws StripeException {
				return Coupon.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // CouponApi

	private final InvoiceApi invoiceApi = new InvoiceApi();

	/**
	 * <h1>Invoices</h1>
	 * <p>
	 * Invoices are statements of what a customer owes for a particular billing
	 * period, including subscriptions, invoice items, and any automatic
	 * proration adjustments if necessary.
	 * </p>
	 * <p>
	 * Once an invoice is created, payment is automatically attempted. Note that
	 * the payment, while automatic, does not happen exactly at the time of
	 * invoice creation. If you have configured webhooks, the invoice will wait
	 * until one hour after the last webhook is successfully sent (or the last
	 * webhook times out after failing).
	 * </p>
	 * <p>
	 * Any customer credit on the account is applied before determining how much
	 * is due for that invoice (the amount that will be actually charged). If
	 * the amount due for the invoice is less than 50 cents (the minimum for a
	 * charge), we add the amount to the customer's running account balance to
	 * be added to the next invoice. If this amount is negative, it will act as
	 * a credit to offset the next invoice. Note that the customer account
	 * balance does not include unpaid invoices; it only includes balances that
	 * need to be taken into account when calculating the amount due for the
	 * next invoice.
	 * </p>
	 */
	public final InvoiceApi invoice() {
		return invoiceApi;
	}

	public final class InvoiceApi {
		/**
		 * <h1>Create an invoice</h1>
		 * <p>
		 * If you need to invoice your customer outside the regular billing
		 * cycle, you can create an invoice that pulls in all pending invoice
		 * items, including prorations. The customer's billing cycle and regular
		 * subscription won't be affected.
		 * </p>
		 * <p>
		 * Once you create the invoice, it'll be picked up and paid
		 * automatically, though you can choose to <a href="#pay_invoice">pay it
		 * right away</a>.
		 * </p>
		 * 
		 * @param customer
		 * 
		 * 
		 */
		public final CreateMethod create(final String customer) {
			return new CreateMethod().set("customer", customer);
		}

		public final class CreateMethod extends Method<Invoice, CreateMethod> {

			@Override
			public final Invoice get() throws StripeException {
				return Invoice.create(params(), options());
			}

			/**
			 * A fee in cents that will be applied to the invoice and
			 * transferred to the application owner’s Stripe account. The
			 * request must be made with an OAuth key or the Stripe-Account
			 * header in order to take an application fee. For more information,
			 * see the application fees <a
			 * href="/docs/connect/collecting-fees#subscriptions"
			 * >documentation</a>.
			 */
			public final CreateMethod applicationFee(final String applicationFee) {
				return set("application_fee", applicationFee);
			}

			/**
			 * 
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Extra information about a charge for the customer’s credit card
			 * statement.
			 */
			public final CreateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * The ID of the subscription to invoice. If not set, the created
			 * invoice will include all pending invoice items for the customer.
			 * If set, the created invoice will exclude pending invoice items
			 * that pertain to other subscriptions.
			 */
			public final CreateMethod subscription(final String subscription) {
				return set("subscription", subscription);
			}

			/**
			 * The percent tax rate applied to the invoice, represented as a
			 * decimal number.
			 */
			public final CreateMethod taxPercent(final String taxPercent) {
				return set("tax_percent", taxPercent);
			}

			/**
			 * 
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve an invoice</h1>
		 * <p>
		 * Retrieves the invoice with the given ID.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the desired invoice.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<Invoice, RetrieveMethod> {

			@Override
			public final Invoice get() throws StripeException {
				return Invoice.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Retrieve an invoice's line items</h1>
		 * <p>
		 * When retrieving an invoice, you'll get a <strong>lines</strong>
		 * property containing the total count of line items and the first
		 * handful of those items. There is also a URL where you can retrieve
		 * the full (paginated) list of line items.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final LinesMethod lines(final Invoice input) {
			return new LinesMethod(input);
		}

		public final class LinesMethod extends
				Method<InvoiceLineItemCollection, LinesMethod> {
			private final Invoice input;

			private LinesMethod(final Invoice input) {
				this.input = input;
			}

			@Override
			public final InvoiceLineItemCollection get() throws StripeException {
				return input.getLines().all(params(), options());
			}

			/**
			 * In the case of upcoming invoices, the customer of the upcoming
			 * invoice is required. In other cases it is ignored.
			 */
			public final LinesMethod customer(final String customer) {
				return set("customer", customer);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final LinesMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * The maximum number of line items to return
			 */
			public final LinesMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final LinesMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

			/**
			 * In the case of upcoming invoices, the subscription of the
			 * upcoming invoice is optional. In other cases it is ignored.
			 */
			public final LinesMethod subscription(final String subscription) {
				return set("subscription", subscription);
			}

			/**
			 * 
			 */
			public final LinesMethod subscriptionPlan(
					final String subscriptionPlan) {
				return set("subscription_plan", subscriptionPlan);
			}

			/**
			 * 
			 */
			public final LinesMethod subscriptionProrate(
					final String subscriptionProrate) {
				return set("subscription_prorate", subscriptionProrate);
			}

			/**
			 * 
			 */
			public final LinesMethod subscriptionProrationDate(
					final String subscriptionProrationDate) {
				return set("subscription_proration_date",
						subscriptionProrationDate);
			}

			/**
			 * 
			 */
			public final LinesMethod subscriptionQuantity(
					final String subscriptionQuantity) {
				return set("subscription_quantity", subscriptionQuantity);
			}

			/**
			 * 
			 */
			public final LinesMethod subscriptionTrialEnd(
					final String subscriptionTrialEnd) {
				return set("subscription_trial_end", subscriptionTrialEnd);
			}

		} // LinesMethod

		/**
		 * <h1>Retrieve an upcoming invoice</h1>
		 * <p>
		 * At any time, you can preview the upcoming invoice for a customer.
		 * This will show you all the charges that are pending, including
		 * subscription renewal charges, invoice item charges, etc. It will also
		 * show you any discount that is applicable to the customer.
		 * </p>
		 * <p>
		 * Note that when you are viewing an upcoming invoice, you are simply
		 * viewing a preview -- the invoice has not yet been created. As such,
		 * the upcoming invoice will not show up in invoice listing calls, and
		 * you cannot use the API to pay or edit the invoice. If you want to
		 * change the amount that your customer will be billed, you can add,
		 * remove, or update pending invoice items, or update the customer's
		 * discount.
		 * </p>
		 * <p>
		 * You can preview the effects of updating a subscription, including a
		 * preview of what proration will take place. To ensure that the actual
		 * proration is calculated exactly the same as the previewed proration,
		 * you should pass a <code>proration_date</code> parameter when doing
		 * the actual subscription update. The value passed in should be the
		 * same as the <code>subscription_proration_date</code> returned on the
		 * upcoming invoice resource. The recommended way to get only the
		 * prorations being previewed is to consider only proration line items
		 * where <code>period[start]</code> is equal to the
		 * <code>subscription_proration_date</code> on the upcoming invoice
		 * resource.
		 * </p>
		 * 
		 * @param customer
		 *            The identifier of the customer whose upcoming invoice
		 *            you'd like to retrieve.
		 * 
		 */
		public final UpcomingMethod upcoming(final String customer) {
			return new UpcomingMethod().set("customer", customer);
		}

		public final class UpcomingMethod extends
				Method<Invoice, UpcomingMethod> {

			@Override
			public final Invoice get() throws StripeException {
				return Invoice.upcoming(params(), options());
			}

			/**
			 * The identifier of the subscription for which you'd like to
			 * retrieve the upcoming invoice. If not provided, but a
			 * <code>subscription_plan</code> is provided, you will preview
			 * creating a subscription to that plan. If neither
			 * <code> subscription</code> nor <code>subscription_plan</code> is
			 * provided, you will retrieve the next upcoming invoice from among
			 * the customer's subscriptions.
			 */
			public final UpcomingMethod subscription(final String subscription) {
				return set("subscription", subscription);
			}

			/**
			 * If set, the invoice returned will preview updating the
			 * subscription given to this plan, or creating a new subscription
			 * to this plan if no <code>subscription</code> is given.
			 */
			public final UpcomingMethod subscriptionPlan(
					final String subscriptionPlan) {
				return set("subscription_plan", subscriptionPlan);
			}

			/**
			 * If provided, the invoice returned will preview updating or
			 * creating a subscription with that quantity. If set, one of
			 * <code>subscription_plan</code> or <code>subscription</code> is
			 * required.
			 */
			public final UpcomingMethod subscriptionQuantity(
					final String subscriptionQuantity) {
				return set("subscription_quantity", subscriptionQuantity);
			}

			/**
			 * If provided, the invoice returned will preview updating or
			 * creating a subscripton with that trial end. If set, one of
			 * <code>subscription_plan</code> or <code>subscription</code> is
			 * required.
			 */
			public final UpcomingMethod subscriptionTrialEnd(
					final String subscriptionTrialEnd) {
				return set("subscription_trial_end", subscriptionTrialEnd);
			}

			/**
			 * If previewing an update to a subscription, this decides whether
			 * the preview will show the result of applying prorations or not.
			 * If set, one of <code>subscription_plan</code> or
			 * <code>subscription</code>, and one of
			 * <code>subscription_plan</code>,
			 * <code>subscription_quantity</code> or
			 * <code>subscription_trial_end</code> are required.
			 */
			public final UpcomingMethod subscriptionProrate(
					final String subscriptionProrate) {
				return set("subscription_prorate", subscriptionProrate);
			}

			/**
			 * If previewing an update to a subscription, and doing proration,
			 * <code>subscription_proration_date</code> forces the proration to
			 * be calculated as though the update was done at the specified
			 * time. The time given must be within the current subscription
			 * period, and cannot be before the subscription was on its current
			 * plan.
			 */
			public final UpcomingMethod subscriptionProrationDate(
					final String subscriptionProrationDate) {
				return set("subscription_proration_date",
						subscriptionProrationDate);
			}

		} // UpcomingMethod

		/**
		 * <h1>Update an invoice</h1>
		 * <p>
		 * Until an invoice is paid, it is marked as open (closed=false). If
		 * you'd like to stop Stripe from automatically attempting payment on an
		 * invoice or would simply like to close the invoice out as no longer
		 * owed by the customer, you can update the closed parameter.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Invoice input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Invoice, UpdateMethod> {
			private final Invoice input;

			private UpdateMethod(final Invoice input) {
				this.input = input;
			}

			@Override
			public final Invoice get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * A fee in cents that will be applied to the invoice and
			 * transferred to the application owner’s Stripe account. The
			 * request must be made with an OAuth key or the Stripe-Account
			 * header in order to take an application fee. For more information,
			 * see the application fees <a
			 * href="/docs/connect/collecting-fees#subscriptions"
			 * >documentation</a>.
			 */
			public final UpdateMethod applicationFee(final String applicationFee) {
				return set("application_fee", applicationFee);
			}

			/**
			 * Boolean representing whether an invoice is closed or not. To
			 * close an invoice, pass true.
			 */
			public final UpdateMethod closed(final String closed) {
				return set("closed", closed);
			}

			/**
			 * 
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Boolean representing whether an invoice is forgiven or not. To
			 * forgive an invoice, pass true. Forgiving an invoice instructs us
			 * to update the subscription status as if the invoice were
			 * succcessfully paid. Once an invoice has been forgiven, it cannot
			 * be unforgiven or reopened.
			 */
			public final UpdateMethod forgiven(final String forgiven) {
				return set("forgiven", forgiven);
			}

			/**
			 * Extra information about a charge for the customer’s credit card
			 * statement.
			 */
			public final UpdateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * The percent tax rate applied to the invoice, represented as a
			 * decimal number. The tax rate of a paid or forgiven invoice cannot
			 * be changed.
			 */
			public final UpdateMethod taxPercent(final String taxPercent) {
				return set("tax_percent", taxPercent);
			}

			/**
			 * 
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Pay an invoice</h1>
		 * <p>
		 * Stripe automatically creates and then attempts to pay invoices for
		 * customers on subscriptions. We'll also retry unpaid invoices
		 * according to your <a
		 * href="https://dashboard.stripe.com/account/recurring">retry
		 * settings</a>. However, if you'd like to attempt to collect payment on
		 * an invoice out of the normal retry schedule or for some other reason,
		 * you can do so.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final PayMethod pay(final Invoice input) {
			return new PayMethod(input);
		}

		public final class PayMethod extends Method<Invoice, PayMethod> {
			private final Invoice input;

			private PayMethod(final Invoice input) {
				this.input = input;
			}

			@Override
			public final Invoice get() throws StripeException {
				return input.pay(options());
			}

		} // PayMethod

		/**
		 * <h1>List all invoices</h1>
		 * <p>
		 * You can list all invoices, or list the invoices for a specific
		 * customer. The invoices are returned sorted by creation date, with the
		 * most recently created invoices appearing first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<InvoiceCollection, AllMethod> {

			@Override
			public final InvoiceCollection get() throws StripeException {
				return Invoice.all(params(), options());
			}

			/**
			 * The identifier of the customer whose invoices to return. If none
			 * is provided, all invoices will be returned.
			 */
			public final AllMethod customer(final String customer) {
				return set("customer", customer);
			}

			/**
			 * A filter on the list based on the object <code>date</code> field.
			 * The value can be a string with an integer Unix timestamp, or it
			 * can be a dictionary with the following options:
			 */
			public final AllMethod date(final String date) {
				return set("date", date);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // InvoiceApi

	private final InvoiceitemApi invoiceitemApi = new InvoiceitemApi();

	/**
	 * <h1>Invoice Items</h1>
	 * <p>
	 * Sometimes you want to add a charge or credit to a customer but only
	 * actually charge the customer's card at the end of a regular billing
	 * cycle. This is useful for combining several charges to minimize
	 * per-transaction fees or having Stripe tabulate your usage-based billing
	 * totals.
	 * </p>
	 */
	public final InvoiceitemApi invoiceitem() {
		return invoiceitemApi;
	}

	public final class InvoiceitemApi {
		/**
		 * <h1>Create an invoice item</h1>
		 * <p>
		 * Adds an arbitrary charge or credit to the customer's upcoming
		 * invoice.
		 * </p>
		 * 
		 * @param customer
		 *            The ID of the customer who will be billed when this
		 *            invoice item is billed.
		 * 
		 * @param amount
		 *            The integer amount in <strong>cents</strong> of the charge
		 *            to be applied to the upcoming invoice. If you want to
		 *            apply a credit to the customer's account, pass a negative
		 *            amount.
		 * 
		 * @param currency
		 *            3-letter <a href=
		 *            "https://support.stripe.com/questions/which-currencies-does-stripe-support"
		 *            >ISO code for currency</a>.
		 * 
		 */
		public final CreateMethod create(final String customer,
				final String amount, final String currency) {
			return new CreateMethod().set("customer", customer)
					.set("amount", amount).set("currency", currency);
		}

		public final class CreateMethod extends
				Method<InvoiceItem, CreateMethod> {

			@Override
			public final InvoiceItem get() throws StripeException {
				return InvoiceItem.create(params(), options());
			}

			/**
			 * The ID of an existing invoice to add this invoice item to. When
			 * left blank, the invoice item will be added to the next upcoming
			 * scheduled invoice. Use this when adding invoice items in response
			 * to an invoice.created webhook. You cannot add an invoice item to
			 * an invoice that has already been paid, attempted or closed.
			 */
			public final CreateMethod invoice(final String invoice) {
				return set("invoice", invoice);
			}

			/**
			 * The ID of a subscription to add this invoice item to. When left
			 * blank, the invoice item will be be added to the next upcoming
			 * scheduled invoice. When set, scheduled invoices for subscriptions
			 * other than the specified subscription will ignore the invoice
			 * item. Use this when you want to express that an invoice item has
			 * been accrued within the context of a particular subscription.
			 */
			public final CreateMethod subscription(final String subscription) {
				return set("subscription", subscription);
			}

			/**
			 * An arbitrary string which you can attach to the invoice item. The
			 * description is displayed in the invoice for easy tracking.
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Controls whether discounts apply to this invoice item. Defaults
			 * to false for prorations or negative invoice items, and true for
			 * all other invoice items.
			 */
			public final CreateMethod discountable(final String discountable) {
				return set("discountable", discountable);
			}

			/**
			 * A set of key/value pairs that you can attach to an invoice item
			 * object. It can be useful for storing additional information about
			 * the invoice item in a structured format.
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve an invoice item</h1>
		 * <p>
		 * Retrieves the invoice item with the given ID.
		 * </p>
		 * 
		 * @param id
		 *            The ID of the desired invoice item.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<InvoiceItem, RetrieveMethod> {

			@Override
			public final InvoiceItem get() throws StripeException {
				return InvoiceItem.retrieve((String) params().get("id"),
						options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update an invoice item</h1>
		 * <p>
		 * Updates the amount or description of an invoice item on an upcoming
		 * invoice. Updating an invoice item is only possible before the invoice
		 * it's attached to is closed.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final InvoiceItem input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends
				Method<InvoiceItem, UpdateMethod> {
			private final InvoiceItem input;

			private UpdateMethod(final InvoiceItem input) {
				this.input = input;
			}

			@Override
			public final InvoiceItem get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * The integer amount in <strong>cents</strong> of the charge to be
			 * applied to the upcoming invoice. If you want to apply a credit to
			 * the customer's account, pass a negative amount.
			 */
			public final UpdateMethod amount(final String amount) {
				return set("amount", amount);
			}

			/**
			 * An arbitrary string which you can attach to the invoice item. The
			 * description is displayed in the invoice for easy tracking. <span
			 * class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * Controls whether discounts apply to this invoice item. Defaults
			 * to false for prorations or negative invoice items, and true for
			 * all other invoice items. Cannot be set to true for prorations.
			 */
			public final UpdateMethod discountable(final String discountable) {
				return set("discountable", discountable);
			}

			/**
			 * A set of key/value pairs that you can attach to an invoice item
			 * object. It can be useful for storing additional information about
			 * the invoice item in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete an invoice item</h1>
		 * <p>
		 * Removes an invoice item from the upcoming invoice. Removing an
		 * invoice item is only possible before the invoice it's attached to is
		 * closed.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final InvoiceItem input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedInvoiceItem, DeleteMethod> {
			private final InvoiceItem input;

			private DeleteMethod(final InvoiceItem input) {
				this.input = input;
			}

			@Override
			public final DeletedInvoiceItem get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all invoice items</h1>
		 * <p>
		 * Returns a list of your invoice items. Invoice items are returned
		 * sorted by creation date, with the most recently created invoice items
		 * appearing first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<InvoiceItemCollection, AllMethod> {

			@Override
			public final InvoiceItemCollection get() throws StripeException {
				return InvoiceItem.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * The identifier of the customer whose invoice items to return. If
			 * none is provided, all invoice items will be returned.
			 */
			public final AllMethod customer(final String customer) {
				return set("customer", customer);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // InvoiceitemApi

	private final DisputeApi disputeApi = new DisputeApi();

	/**
	 * <h1>Disputes</h1>
	 * <p>
	 * A dispute occurs when a customer questions your charge with their bank or
	 * credit card company. When a customer disputes your charge, you're given
	 * the opportunity to respond to the dispute with evidence that shows the
	 * charge is legitimate. You can find more information about the dispute
	 * process in our <a href="/help/disputes">disputes FAQ</a>.
	 * </p>
	 */
	public final DisputeApi dispute() {
		return disputeApi;
	}

	public final class DisputeApi {
		/**
		 * <h1>Retrieve a dispute</h1>
		 * <p>
		 * Retrieves the dispute with the given ID.
		 * </p>
		 * 
		 * @param dispute
		 * 
		 * 
		 */
		public final RetrieveMethod retrieve(final String dispute) {
			return new RetrieveMethod().set("dispute", dispute);
		}

		public final class RetrieveMethod extends
				Method<Dispute, RetrieveMethod> {

			@Override
			public final Dispute get() throws StripeException {
				return Dispute.retrieve((String) params().get("dispute"),
						options());
			}

		} // RetrieveMethod

		/**
		 * <h3>Updating a dispute</h3>
		 * 
		 * <p>
		 * When you get a dispute, contacting your customer is always the best
		 * first step. If that doesn't work, you can submit evidence in order to
		 * help us resolve the dispute in your favor. You can do this in your <a
		 * href="https://dashboard.stripe.com/#disputes">dashboard</a>, but if
		 * you prefer, you can use the API to submit evidence programmatically.
		 * </p>
		 * 
		 * <p>
		 * Depending on your dispute type, different evidence fields will give
		 * you a better chance of winning your dispute. You may want to consult
		 * our <a href="https://stripe.com/help/dispute-types">guide to dispute
		 * types</a> to help you figure out which evidence fields to provide.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param dispute
		 * 
		 * 
		 */
		public final UpdateMethod update(final Dispute input,
				final String dispute) {
			return new UpdateMethod(input).set("dispute", dispute);
		}

		public final class UpdateMethod extends Method<Dispute, UpdateMethod> {
			private final Dispute input;

			private UpdateMethod(final Dispute input) {
				this.input = input;
			}

			@Override
			public final Dispute get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * Evidence to upload to respond to a dispute. Updating any field in
			 * the hash will submit all fields in the hash for review.
			 */
			public final UpdateMethod evidence(final String evidence) {
				return set("evidence", evidence);
			}

			/**
			 * A set of key/value pairs that you can attach to a dispute object.
			 * It can be useful for storing additional information about the
			 * dispute in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Close a dispute</h1>
		 * <p>
		 * Closing the dispute for a charge indicates that you do not have any
		 * evidence to submit and are essentially 'dismissing' the dispute,
		 * acknowledging it as lost.
		 * </p>
		 * <p>
		 * The status of the dispute will change from <code>under_review</code>
		 * to <code>lost</code>. <strong>Closing a dispute is
		 * irreversible.</strong>
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param dispute
		 * 
		 * 
		 */
		public final CloseMethod close(final Dispute input, final String dispute) {
			return new CloseMethod(input).set("dispute", dispute);
		}

		public final class CloseMethod extends Method<Dispute, CloseMethod> {
			private final Dispute input;

			private CloseMethod(final Dispute input) {
				this.input = input;
			}

			@Override
			public final Dispute get() throws StripeException {
				return input.close(options());
			}

		} // CloseMethod

		/**
		 * <h1>List all disputes</h1>
		 * <p>
		 * Returns a list of your disputes.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<DisputeCollection, AllMethod> {

			@Override
			public final DisputeCollection get() throws StripeException {
				return Dispute.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // DisputeApi

	private final TransferApi transferApi = new TransferApi();

	/**
	 * <h1>Transfers</h1>
	 * <p>
	 * When Stripe sends you money or you initiate a transfer to a bank account,
	 * debit card, or connected Stripe account, a transfer object will be
	 * created. You can retrieve individual transfers as well as list all
	 * transfers.
	 * </p>
	 * <p>
	 * View the <a href="/docs/tutorials/sending-transfers">documentation</a> on
	 * creating transfers via the API.
	 * </p>
	 */
	public final TransferApi transfer() {
		return transferApi;
	}

	public final class TransferApi {
		/**
		 * <h1>Create a transfer</h1>
		 * <p>
		 * To send funds from your Stripe account to a third-party recipient or
		 * to your own bank account, you create a new transfer object. Your <a
		 * href="#balance">Stripe balance</a> must be able to cover the transfer
		 * amount, or you'll receive an "Insufficient Funds" error.
		 * </p>
		 * <p>
		 * If your API key is in test mode, money won't actually be sent, though
		 * everything else will occur as if in live mode.
		 * </p>
		 * 
		 * @param amount
		 *            A positive integer in <strong>cents</strong> representing
		 *            how much to transfer.
		 * 
		 * @param currency
		 *            3-letter <a href=
		 *            "https://support.stripe.com/questions/which-currencies-does-stripe-support"
		 *            >ISO code for currency</a>.
		 * 
		 * @param destination
		 *            The id of a bank account or a card to send the transfer
		 *            to, or the string <code>default_for_currency</code> to use
		 *            the default external account for the specified currency. <br>
		 * <br>
		 *            If you use Stripe Connect, this can be the the id of a
		 *            connected Stripe account; <a
		 *            href="/docs/connect/special-case-transfers">see the
		 *            details</a> about when such transfers are permitted.
		 * 
		 */
		public final CreateMethod create(final String amount,
				final String currency, final String destination) {
			return new CreateMethod().set("amount", amount)
					.set("currency", currency).set("destination", destination);
		}

		public final class CreateMethod extends Method<Transfer, CreateMethod> {

			@Override
			public final Transfer get() throws StripeException {
				return Transfer.create(params(), options());
			}

			/**
			 * You can use this parameter to transfer funds from a charge (or
			 * other transaction) before they are added to your available
			 * balance. The transfer will take on the pending status of the
			 * charge: if the funds from the charge become available in N days,
			 * the payment that the destination account receives from the
			 * transfer will also become available in N days. <a href=
			 * "/docs/connect/special-case-transfers#associating-a-transfer-with-a-charge"
			 * >See the Connect documentation</a> for details.
			 */
			public final CreateMethod sourceTransaction(
					final String sourceTransaction) {
				return set("source_transaction", sourceTransaction);
			}

			/**
			 * An arbitrary string which you can attach to a transfer object. It
			 * is displayed when in the web interface alongside the transfer.
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * An arbitrary string which will be displayed on the recipient's
			 * bank or card statement. This may be at most <strong>22
			 * characters</strong>. Attempting to use a
			 * <code>statement_descriptor</code> longer than 22 characters will
			 * return an error. <strong>Note:</strong> While most banks display
			 * this information consistently, some may display it incorrectly or
			 * not at all.
			 */
			public final CreateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * A set of key/value pairs that you can attach to a transfer
			 * object. It can be useful for storing additional information about
			 * the transfer in a structured format.
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a transfer</h1>
		 * <p>
		 * Retrieves the details of an existing transfer. Supply the unique
		 * transfer ID from either a transfer creation request or the transfer
		 * list, and Stripe will return the corresponding transfer information.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the transfer to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<Transfer, RetrieveMethod> {

			@Override
			public final Transfer get() throws StripeException {
				return Transfer
						.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a transfer</h1>
		 * <p>
		 * Updates the specified transfer by setting the values of the
		 * parameters passed. Any parameters not provided will be left
		 * unchanged.
		 * </p>
		 * <p>
		 * This request accepts only the description and metadata as arguments.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Transfer input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Transfer, UpdateMethod> {
			private final Transfer input;

			private UpdateMethod(final Transfer input) {
				this.input = input;
			}

			@Override
			public final Transfer get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * An arbitrary string which you can attach to a transfer object. It
			 * is displayed when in the web interface alongside the transfer.
			 * <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * A set of key/value pairs that you can attach to a transfer
			 * object. It can be useful for storing additional information about
			 * the transfer in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>List all transfers</h1>
		 * <p>
		 * Returns a list of existing transfers sent to third-party bank
		 * accounts or that Stripe has sent you. The transfers are returned in
		 * sorted order, with the most recently created transfers appearing
		 * first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<TransferCollection, AllMethod> {

			@Override
			public final TransferCollection get() throws StripeException {
				return Transfer.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A filter on the list based on the object <code>date</code> field.
			 * The value can be a string with an integer Unix timestamp, or it
			 * can be a dictionary with the following options:
			 */
			public final AllMethod date(final String date) {
				return set("date", date);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * Only return transfers for the recipient specified by this
			 * recipient ID.
			 */
			public final AllMethod recipient(final String recipient) {
				return set("recipient", recipient);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

			/**
			 * Only return transfers that have the given status:
			 * <code>pending</code>, <code>paid</code>, <code>failed</code>,
			 * <code>in_transit</code>, or <code>canceled</code>.
			 */
			public final AllMethod status(final String status) {
				return set("status", status);
			}

		} // AllMethod

	} // TransferApi

	private final ReversalApi reversalApi = new ReversalApi();

	/**
	 * <h1>Transfer Reversals</h1>
	 * <p>
	 * A previously created transfer can be reversed if it has not yet been paid
	 * out. Funds will be refunded to your available balance, and the fees you
	 * were originally charged on the transfer will be refunded. You may not
	 * reverse automatic Stripe transfers.
	 * </p>
	 */
	public final ReversalApi reversal() {
		return reversalApi;
	}

	public final class ReversalApi {
		/**
		 * <h1>Create a transfer reversal</h1>
		 * <p>
		 * When you create a new reversal, you must specify a transfer to create
		 * it on.
		 * </p>
		 * <p>
		 * Creating a new reversal on a transfer that has previously been
		 * created but not paid out will return the funds to your available
		 * balance and refund the fees you were originally charged on the
		 * transfer. You may not reverse automatic Stripe transfers.
		 * </p>
		 * <p>
		 * When reversing transfers to Stripe accounts, you can optionally
		 * reverse part of the transfer. You can do so as many times as you wish
		 * until the entire transfer has been reversed.
		 * </p>
		 * <p>
		 * Once entirely reversed, a transfer can't be reversed again. This
		 * method will return an error when called on an already-reversed
		 * transfer, or when trying to reverse more money than is left on a
		 * transfer.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final CreateMethod create(final Transfer input) {
			return new CreateMethod(input);
		}

		public final class CreateMethod extends Method<Reversal, CreateMethod> {
			private final Transfer input;

			private CreateMethod(final Transfer input) {
				this.input = input;
			}

			@Override
			public final Reversal get() throws StripeException {
				return input.getReversals().create(params(), options());
			}

			/**
			 * A positive integer in <strong>cents</strong> representing how
			 * much of this transfer to reverse. Can only reverse up to the
			 * unreversed amount remaining of the transfer. Partial transfer
			 * reversals are only allowed for transfers to Stripe Accounts.
			 */
			public final CreateMethod amount(final String amount) {
				return set("amount", amount);
			}

			/**
			 * Boolean indicating whether the application fee should be refunded
			 * when reversing this transfer. If a full transfer reversal is
			 * given, the full application fee will be refunded. Otherwise, the
			 * application fee will be refunded with an amount proportional to
			 * the amount of the transfer reversed.
			 */
			public final CreateMethod refundApplicationFee(
					final String refundApplicationFee) {
				return set("refund_application_fee", refundApplicationFee);
			}

			/**
			 * An arbitrary string which you can attach to a reversal object. It
			 * is displayed alongside the reversal in the dashboard. This will
			 * be unset if you POST an empty value.
			 */
			public final CreateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * A set of key/value pairs that you can attach to a reversal
			 * object. It can be useful for storing additional information about
			 * the reversal in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a reversal</h1>
		 * <p>
		 * By default, you can see the 10 most recent reversals stored directly
		 * on the transfer object, but you can also retrieve details about a
		 * specific reversal stored on the transfer.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 *            ID of reversal to retrieve.
		 * 
		 * @param transfer
		 *            ID of the transfer reversed.
		 * 
		 */
		public final RetrieveMethod retrieve(final Transfer input,
				final String id, final String transfer) {
			return new RetrieveMethod(input).set("id", id).set("transfer",
					transfer);
		}

		public final class RetrieveMethod extends
				Method<Reversal, RetrieveMethod> {
			private final Transfer input;

			private RetrieveMethod(final Transfer input) {
				this.input = input;
			}

			@Override
			public final Reversal get() throws StripeException {
				return input.getReversals().retrieve(
						(String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a reversal</h1>
		 * <p>
		 * Updates the specified reversal by setting the values of the
		 * parameters passed. Any parameters not provided will be left
		 * unchanged.
		 * </p>
		 * <p>
		 * This request only accepts metadata and description as arguments.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Reversal input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Reversal, UpdateMethod> {
			private final Reversal input;

			private UpdateMethod(final Reversal input) {
				this.input = input;
			}

			@Override
			public final Reversal get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * An arbitrary string which you can attach to a reversal object. It
			 * is displayed when in the web interface alongside the reversal.
			 * <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod description(final String description) {
				return set("description", description);
			}

			/**
			 * A set of key/value pairs that you can attach to a reversal
			 * object. It can be useful for storing additional information about
			 * the reversal in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>List all reversals</h1>
		 * <p>
		 * You can see a list of the reversals belonging to a specific transfer.
		 * Note that the 10 most recent reversals are always available by
		 * default on the transfer object. If you need more than those 10, you
		 * can use this API method and the <code>limit</code> and
		 * <code>starting_after</code> parameters to page through additional
		 * reversals.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final Transfer input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<TransferReversalCollection, AllMethod> {
			private final Transfer input;

			private AllMethod(final Transfer input) {
				this.input = input;
			}

			@Override
			public final TransferReversalCollection get()
					throws StripeException {
				return input.getReversals().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // ReversalApi

	private final BankAccountApi bankAccountApi = new BankAccountApi();

	/**
	 * <h1>Bank Accounts</h1>
	 * <p>
	 * You can store multiple bank accounts on a <a
	 * href="/docs/connect/managed-accounts">managed account</a> in order to
	 * transfer to those bank accounts later.
	 * </p>
	 */
	public final BankAccountApi bankAccount() {
		return bankAccountApi;
	}

	public final class BankAccountApi {
		/**
		 * <h1>Create a bank account</h1>
		 * <p>
		 * When you create a new bank account, you must specify a <a
		 * href="/docs/connect/managed-accounts">managed account</a> to create
		 * it on.
		 * </p>
		 * <p>
		 * If the bank accounts's owner has no other external account in the
		 * bank account's currency, the new bank account will become the default
		 * for that currency. However, if the owner already has a bank account
		 * for that currency, the new account will only become the default if
		 * the <code>default_for_currency</code> parameter is set to
		 * <code>true</code>.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param externalAccount
		 *            This can either be a token, like the ones returned by our
		 *            <a href="/docs/stripe.js">Stripe.js</a>, or a dictionary
		 *            containing a user’s bank account details (with the options
		 *            shown below).
		 * 
		 */
		public final CreateMethod create(final Account input,
				final String externalAccount) {
			return new CreateMethod(input).set("external_account",
					externalAccount);
		}

		public final class CreateMethod extends
				Method<ExternalAccount, CreateMethod> {
			private final Account input;

			private CreateMethod(final Account input) {
				this.input = input;
			}

			@Override
			public final ExternalAccount get() throws StripeException {
				return input.getExternalAccounts().create(params(), options());
			}

			/**
			 * If you set this to true (or if this is the first external account
			 * being added in this currency) this bank account will become the
			 * default external account for its currency.
			 */
			public final CreateMethod defaultForCurrency(
					final String defaultForCurrency) {
				return set("default_for_currency", defaultForCurrency);
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve a bank account</h1>
		 * <p>
		 * By default, you can see the 10 most recent bank accounts stored on a
		 * <a href="/docs/connect/managed-accounts">managed account</a> directly
		 * on the Stripe account object, but you can also retrieve details about
		 * a specific bank account stored on the Stripe account.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 * 
		 * 
		 */
		public final RetrieveMethod retrieve(final Account input,
				final String id) {
			return new RetrieveMethod(input).set("id", id);
		}

		public final class RetrieveMethod extends
				Method<ExternalAccount, RetrieveMethod> {
			private final Account input;

			private RetrieveMethod(final Account input) {
				this.input = input;
			}

			@Override
			public final ExternalAccount get() throws StripeException {
				return input.getExternalAccounts().retrieve(
						(String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update a bank account</h1>
		 * <p>
		 * Updates the metadata of a bank account (belonging to a <a
		 * href="/docs/connect/managed-accounts">managed account</a>) and
		 * optionally sets it as the default for its currency. Other bank
		 * account details are, by design, not editable.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final BankAccount input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends
				Method<ExternalAccount, UpdateMethod> {
			private final BankAccount input;

			private UpdateMethod(final BankAccount input) {
				this.input = input;
			}

			@Override
			public final ExternalAccount get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * If set to <code>true</code>, this bank account will become the
			 * default external account for its currency.
			 */
			public final UpdateMethod defaultForCurrency(
					final String defaultForCurrency) {
				return set("default_for_currency", defaultForCurrency);
			}

			/**
			 * 
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete a bank account</h1>
		 * <p>
		 * You can delete bank accounts from a <a
		 * href="/docs/connect/managed-accounts">managed account</a>. If a bank
		 * account is the default external account for its currency, it can only
		 * be deleted if it is the only external account for that currency, and
		 * the currency is not the Stripe account's default currency. Otherwise,
		 * you must set another external account to be the default for the
		 * currency before deleting it.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final BankAccount input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedBankAccount, DeleteMethod> {
			private final BankAccount input;

			private DeleteMethod(final BankAccount input) {
				this.input = input;
			}

			@Override
			public final DeletedBankAccount get() throws StripeException {
				return input.delete(options());
			}

		} // DeleteMethod

		/**
		 * <h1>List all bank accounts</h1>
		 * <p>
		 * You can see a list of the bank accounts belonging to a <a
		 * href="/docs/connect/managed-accounts">managed account</a>. Note that
		 * the 10 most recent external accounts are always available by default
		 * on the Stripe account object. If you need more than those 10, you can
		 * use this API method and the <code>limit</code> and
		 * <code>starting_after</code> parameters to page through additional
		 * bank accounts.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final Account input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<ExternalAccountCollection, AllMethod> {
			private final Account input;

			private AllMethod(final Account input) {
				this.input = input;
			}

			@Override
			public final ExternalAccountCollection get() throws StripeException {
				return input.getExternalAccounts().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // BankAccountApi

	private final ApplicationFeeApi applicationFeeApi = new ApplicationFeeApi();

	/**
	 * <h1>Application Fees</h1>
	 * <p>
	 * When you collect a transaction fee on top of a charge made for your user
	 * (using <a href="/docs/connect">Stripe Connect</a>), an application fee
	 * object is created in your account. You can list, retrieve, and refund
	 * application fees.
	 * </p>
	 * <p>
	 * For more information on collecting transaction fees, see our <a
	 * href="/docs/connect/collecting-fees">documentation</a>.
	 * </p>
	 */
	public final ApplicationFeeApi applicationFee() {
		return applicationFeeApi;
	}

	public final class ApplicationFeeApi {
		/**
		 * <h1>Retrieve an application fee</h1>
		 * <p>
		 * Retrieves the details of an application fee that your account has
		 * collected. The same information is returned when refunding the
		 * application fee.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the fee to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends
				Method<ApplicationFee, RetrieveMethod> {

			@Override
			public final ApplicationFee get() throws StripeException {
				return ApplicationFee.retrieve((String) params().get("id"),
						options());
			}

		} // RetrieveMethod

		/**
		 * <h1>List all application fees</h1>
		 * <p>
		 * Returns a list of application fees you've previously collected. The
		 * application fees are returned in sorted order, with the most recent
		 * fees appearing first.
		 * </p>
		 * <p>
		 * To retrieve application fees associated with a specific charge, you
		 * should filter by that charge ID.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<ApplicationFeeCollection, AllMethod> {

			@Override
			public final ApplicationFeeCollection get() throws StripeException {
				return ApplicationFee.all(params(), options());
			}

			/**
			 * Only return application fees for the charge specified by this
			 * charge ID.
			 */
			public final AllMethod charge(final String charge) {
				return set("charge", charge);
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // ApplicationFeeApi

	private final FeeRefundApi feeRefundApi = new FeeRefundApi();

	/**
	 * <h1>Application Fee Refunds</h1>
	 * <p>
	 * Application Fee Refund objects allow you to refund an application fee
	 * that has previously been created but not yet refunded. Funds will be
	 * refunded to the Stripe account that the fee was originally collected
	 * from.
	 * </p>
	 */
	public final FeeRefundApi feeRefund() {
		return feeRefundApi;
	}

	public final class FeeRefundApi {
		/**
		 * <h1>Create an application fee refund</h1>
		 * <p>
		 * Refunds an application fee that has previously been collected but not
		 * yet refunded. Funds will be refunded to the Stripe account that the
		 * fee was originally collected from.
		 * </p>
		 * <p>
		 * You can optionally refund only part of an application fee. You can do
		 * so as many times as you wish until the entire fee has been refunded.
		 * </p>
		 * <p>
		 * Once entirely refunded, an application fee can't be refunded again.
		 * This method will <span class="lang lang-java">throw</span> an error
		 * when called on an already-refunded application fee, or when trying to
		 * refund more money than is left on an application fee.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final CreateMethod create(final ApplicationFee input) {
			return new CreateMethod(input);
		}

		public final class CreateMethod extends
				Method<ApplicationFee, CreateMethod> {
			private final ApplicationFee input;

			private CreateMethod(final ApplicationFee input) {
				this.input = input;
			}

			@Override
			public final ApplicationFee get() throws StripeException {
				return input.refund(params(), options());
			}

			/**
			 * A positive integer in <strong>cents</strong> representing how
			 * much of this fee to refund. Can only refund up to the unrefunded
			 * amount remaining of the fee.
			 */
			public final CreateMethod amount(final String amount) {
				return set("amount", amount);
			}

			/**
			 * A set of key/value pairs that you can attach to a refund object.
			 * It can be useful for storing additional information about the
			 * refund in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final CreateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve an application fee refund</h1>
		 * <p>
		 * By default, you can see the 10 most recent refunds stored directly on
		 * the application fee object, but you can also retrieve details about a
		 * specific refund stored on the application fee.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 * @param id
		 *            ID of refund to retrieve.
		 * 
		 * @param fee
		 *            ID of the application fee refunded.
		 * 
		 */
		public final RetrieveMethod retrieve(final ApplicationFee input,
				final String id, final String fee) {
			return new RetrieveMethod(input).set("id", id).set("fee", fee);
		}

		public final class RetrieveMethod extends
				Method<FeeRefund, RetrieveMethod> {
			private final ApplicationFee input;

			private RetrieveMethod(final ApplicationFee input) {
				this.input = input;
			}

			@Override
			public final FeeRefund get() throws StripeException {
				return input.getRefunds().retrieve((String) params().get("id"),
						options());
			}

		} // RetrieveMethod

		/**
		 * <h1>Update an application fee refund</h1>
		 * <p>
		 * Updates the specified application fee refund by setting the values of
		 * the parameters passed. Any parameters not provided will be left
		 * unchanged.
		 * </p>
		 * <p>
		 * This request only accepts metadata as an argument.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Refund input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Refund, UpdateMethod> {
			private final Refund input;

			private UpdateMethod(final Refund input) {
				this.input = input;
			}

			@Override
			public final Refund get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * A set of key/value pairs that you can attach to an application
			 * fee refund object. It can be useful for storing additional
			 * information about the refund in a structured format. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >You can unset an individual key by setting its value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving. To clear all keys, set metadata to
			 * <tt><span class="lang lang-java">null</span></tt>, then
			 * save.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>List all application fee refunds</h1>
		 * <p>
		 * You can see a list of the refunds belonging to a specific application
		 * fee. Note that the 10 most recent refunds are always available by
		 * default on the application fee object. If you need more than those
		 * 10, you can use this API method and the <code>limit</code> and
		 * <code>starting_after</code> parameters to page through additional
		 * refunds.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final AllMethod all(final ApplicationFee input) {
			return new AllMethod(input);
		}

		public final class AllMethod extends
				Method<FeeRefundCollection, AllMethod> {
			private final ApplicationFee input;

			private AllMethod(final ApplicationFee input) {
				this.input = input;
			}

			@Override
			public final FeeRefundCollection get() throws StripeException {
				return input.getRefunds().all(params(), options());
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

		} // AllMethod

	} // FeeRefundApi

	private final AccountApi accountApi = new AccountApi();

	/**
	 * <h1>Account</h1>
	 * <p>
	 * This is an object representing your Stripe account. You can retrieve it
	 * to see properties on the account like its current e-mail address or if
	 * the account is enabled yet to make live charges.
	 * </p>
	 * 
	 * <p>
	 * Some properties, marked as "managed accounts only", are only available to
	 * platforms who want to <a
	 * href="/docs/connect/connecting-to-accounts">create and manage Stripe
	 * accounts</a>.
	 * </p>
	 */
	public final AccountApi account() {
		return accountApi;
	}

	public final class AccountApi {
		/**
		 * <h1>Create an account</h1>
		 * <p>
		 * With <a href="/docs/connect">Connect</a>, you can create Stripe
		 * accounts for your users. To do this, you'll first need to <a
		 * href="https://dashboard.stripe.com/account/application/settings"
		 * >register your platform</a>.
		 * </p>
		 * 
		 */
		public final CreateMethod create() {
			return new CreateMethod();
		}

		public final class CreateMethod extends Method<Account, CreateMethod> {

			@Override
			public final Account get() throws StripeException {
				return Account.create(params(), options());
			}

			/**
			 * Whether you'd like to create a <a
			 * href="/docs/connect/connecting-to-accounts">managed or
			 * standalone</a> account. Managed accounts have extra parameters
			 * available to them, and require that you, the platform, handle all
			 * communication with the account holder. Standalone accounts are
			 * normal Stripe accounts: Stripe will email the account holder to
			 * setup a username and password, and handle all account management
			 * directly with them.
			 */
			public final CreateMethod managed(final String managed) {
				return set("managed", managed);
			}

			/**
			 * The country the account holder resides in or that the business is
			 * legally established in. For example, if you are in the United
			 * States and the business you’re creating an account for is legally
			 * represented in Canada, you would use “CA” as the country for the
			 * account being created.
			 */
			public final CreateMethod country(final String country) {
				return set("country", country);
			}

			/**
			 * The email address of the account holder. For standalone accounts,
			 * Stripe will email your user with instructions for how to set up
			 * their account. For managed accounts, this is only to make the
			 * account easier to identify to you: Stripe will never directly
			 * reach out to your users.
			 */
			public final CreateMethod email(final String email) {
				return set("email", email);
			}

		} // CreateMethod

		/**
		 * <h1>Retrieve account details</h1>
		 * <p>
		 * Retrieves the details of the account.
		 * </p>
		 * 
		 */
		public final RetrieveMethod retrieve() {
			return new RetrieveMethod();
		}

		public final class RetrieveMethod extends
				Method<Account, RetrieveMethod> {

			@Override
			public final Account get() throws StripeException {
				return Account.retrieve((String) params().get("id"), options());
			}

			/**
			 * The identifier of the account to be retrieved. If none is
			 * provided, will default to the account of the API key.
			 */
			public final RetrieveMethod id(final String id) {
				return set("id", id);
			}

		} // RetrieveMethod

		/**
		 * <h1>Update an account</h1>
		 * <p>
		 * Updates an account by setting the values of the parameters passed.
		 * Any parameters not provided will be left unchanged.
		 * </p>
		 * <p>
		 * <strong>You may only update accounts that you <a
		 * href="/docs/connect/managed-accounts">manage</a></strong>. To update
		 * your own account, you can currently only do so via the <a
		 * href="https://dashboard.stripe.com/account">dashboard</a>. For more
		 * information on updating managed accounts, see <a
		 * href="/docs/connect/updating-accounts">our guide</a>.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final UpdateMethod update(final Account input) {
			return new UpdateMethod(input);
		}

		public final class UpdateMethod extends Method<Account, UpdateMethod> {
			private final Account input;

			private UpdateMethod(final Account input) {
				this.input = input;
			}

			@Override
			public final Account get() throws StripeException {
				return input.update(params(), options());
			}

			/**
			 * 
			 */
			public final UpdateMethod businessLogo(final String businessLogo) {
				return set("business_logo", businessLogo);
			}

			/**
			 * The publicly sharable name for this account
			 */
			public final UpdateMethod businessName(final String businessName) {
				return set("business_name", businessName);
			}

			/**
			 * A CSS hex color value representing the primary branding color for
			 * this account
			 */
			public final UpdateMethod businessPrimaryColor(
					final String businessPrimaryColor) {
				return set("business_primary_color", businessPrimaryColor);
			}

			/**
			 * The URL that best shows the service or product provided for this
			 * account
			 */
			public final UpdateMethod businessUrl(final String businessUrl) {
				return set("business_url", businessUrl);
			}

			/**
			 * A publicly shareable email address that can be reached for
			 * support for this account
			 */
			public final UpdateMethod supportEmail(final String supportEmail) {
				return set("support_email", supportEmail);
			}

			/**
			 * A publicly shareable phone number that can be reached for support
			 * for this account
			 */
			public final UpdateMethod supportPhone(final String supportPhone) {
				return set("support_phone", supportPhone);
			}

			/**
			 * A publicly shareable URL that can be reached for support for this
			 * account
			 */
			public final UpdateMethod supportUrl(final String supportUrl) {
				return set("support_url", supportUrl);
			}

			/**
			 * A boolean for whether or not Stripe should try to reclaim
			 * negative balances from the account holder’s bank account. See our
			 * <a href="/docs/connect/bank-transfers#negative-balances">managed
			 * account bank transfer guide</a> for more information
			 */
			public final UpdateMethod debitNegativeBalances(
					final String debitNegativeBalances) {
				return set("debit_negative_balances", debitNegativeBalances);
			}

			/**
			 * Account-level settings to automatically decline certain types of
			 * charges regardless of the bank’s decision.
			 */
			public final UpdateMethod declineChargeOn(
					final String declineChargeOn) {
				return set("decline_charge_on", declineChargeOn);
			}

			/**
			 * Three-letter ISO currency code representing the default currency
			 * for the account. This must be a currency that <a href=
			 * "https://support.stripe.com/questions/which-currencies-does-stripe-support"
			 * >Stripe supports in the account’s country</a>.
			 */
			public final UpdateMethod defaultCurrency(
					final String defaultCurrency) {
				return set("default_currency", defaultCurrency);
			}

			/**
			 * Email address of the account holder. For standalone accounts,
			 * this is used to email them asking them to claim their Stripe
			 * account. For managed accounts, this is only to make the account
			 * easier to identify to you: Stripe will not email the account
			 * holder.
			 */
			public final UpdateMethod email(final String email) {
				return set("email", email);
			}

			/**
			 * A card or bank account to attach to the account. You can provide
			 * either a token, like the ones returned by <a
			 * href="/docs/stripe.js">Stripe.js</a>, or a dictionary as
			 * documented in the external_account parameter for either <a
			 * href="/docs/api#create_card">card</a> or <a
			 * href="/docs/api#create_bank_account">bank account</a> creation. <br>
			 * <br>
			 * This will create a new external account object, make it the new
			 * default external account for its currency, and delete the old
			 * default if one exists. If you want to add additional external
			 * accounts instead of replacing the existing default for this
			 * currency, use the bank account or card creation API.
			 */
			public final UpdateMethod externalAccount(
					final String externalAccount) {
				return set("external_account", externalAccount);
			}

			/**
			 * Information about the holder of this account, i.e. the user
			 * receiving funds from this account
			 */
			public final UpdateMethod legalEntity(final String legalEntity) {
				return set("legal_entity", legalEntity);
			}

			/**
			 * Internal-only description of the product being sold or service
			 * being provided by this account. It’s used by Stripe for risk and
			 * underwriting purposes.
			 */
			public final UpdateMethod productDescription(
					final String productDescription) {
				return set("product_description", productDescription);
			}

			/**
			 * The text that will appear on credit card statements by default if
			 * a charge is being made <a
			 * href="/docs/connect/payments-fees#charging-directly">directly on
			 * the account</a>.
			 */
			public final UpdateMethod statementDescriptor(
					final String statementDescriptor) {
				return set("statement_descriptor", statementDescriptor);
			}

			/**
			 * Details on who accepted the Stripe terms of service, and when
			 * they accepted it. See our <a
			 * href="/docs/connect/updating-accounts#tos_acceptance">updating
			 * managed accounts guide</a> for more information
			 */
			public final UpdateMethod tosAcceptance(final String tosAcceptance) {
				return set("tos_acceptance", tosAcceptance);
			}

			/**
			 * Details on when this account will make funds from charges
			 * available, and when they will be paid out to the account holder’s
			 * bank account. See our <a
			 * href="/docs/connect/bank-transfers#payout-information">managed
			 * account bank transfer guide</a> for more information
			 */
			public final UpdateMethod transferSchedule(
					final String transferSchedule) {
				return set("transfer_schedule", transferSchedule);
			}

			/**
			 * A set of key/value pairs that you can attach to an account
			 * object. It can be useful for storing additional information about
			 * the account in a structured format. This will be unset if you
			 * POST an empty value. <span class=
			 * "lang lang-ruby lang-python lang-php lang-java lang-node lang-go"
			 * >This can be unset by updating the value to
			 * <tt><span class="lang lang-java">null</span></tt> and then
			 * saving.</span>
			 */
			public final UpdateMethod metadata(final String... pairs) {
				return set("metadata", map(pairs));
			}

		} // UpdateMethod

		/**
		 * <h1>Delete an account</h1>
		 * <p>
		 * With <a href="/docs/connect">Connect</a>, you may delete Stripe
		 * accounts you manage.
		 * </p>
		 * <p>
		 * Managed accounts created using test-mode keys can be deleted at any
		 * time. Managed accounts created using live-mode keys may only be
		 * deleted once all balances are zero.
		 * </p>
		 * <p>
		 * If you are looking to close your own account, use the <a
		 * href="https://dashboard.stripe.com/account/data">data tab in your
		 * account settings</a> instead.
		 * </p>
		 * 
		 * @param input
		 *            Input object for this operation.
		 * 
		 */
		public final DeleteMethod delete(final Account input) {
			return new DeleteMethod(input);
		}

		public final class DeleteMethod extends
				Method<DeletedAccount, DeleteMethod> {
			private final Account input;

			private DeleteMethod(final Account input) {
				this.input = input;
			}

			@Override
			public final DeletedAccount get() throws StripeException {
				return input.delete(params(), options());
			}

		} // DeleteMethod

	} // AccountApi

	private final BalanceApi balanceApi = new BalanceApi();

	/**
	 * <h1>Balance</h1>
	 * <p>
	 * This is an object representing your Stripe balance. You can retrieve it
	 * to see the balance currently on your Stripe account.
	 * </p>
	 * <p>
	 * You can also retrieve a list of the balance history, which contains a
	 * full list of transactions that have ever contributed to the balance
	 * (charges, refunds, transfers, and so on).
	 * </p>
	 */
	public final BalanceApi balance() {
		return balanceApi;
	}

	public final class BalanceApi {
		/**
		 * <h1>Retrieve balance</h1>
		 * <p>
		 * Retrieves the current account balance, based on the API key that was
		 * used to make the request.
		 * </p>
		 * 
		 */
		public final RetrieveMethod retrieve() {
			return new RetrieveMethod();
		}

		public final class RetrieveMethod extends
				Method<Balance, RetrieveMethod> {

			@Override
			public final Balance get() throws StripeException {
				return Balance.retrieve(options());
			}

			/**
			 * 
			 */
			public final RetrieveMethod no(final String no) {
				return set("No", no);
			}

		} // RetrieveMethod

		/**
		 * <h1>Retrieve a balance transaction</h1>
		 * <p>
		 * Retrieves the balance transaction with the given ID.
		 * </p>
		 * 
		 * @param id
		 *            The ID of the desired balance transaction (as found on any
		 *            API object that affects the balance, e.g. a charge or
		 *            transfer).
		 * 
		 */
		public final RetrieveTransactionMethod retrieveTransaction(
				final String id) {
			return new RetrieveTransactionMethod().set("id", id);
		}

		public final class RetrieveTransactionMethod extends
				Method<BalanceTransaction, RetrieveTransactionMethod> {

			@Override
			public final BalanceTransaction get() throws StripeException {
				return BalanceTransaction.retrieve((String) params().get("id"),
						options());
			}

		} // RetrieveTransactionMethod

		/**
		 * <h1>List all balance history</h1>
		 * <p>
		 * Returns a list of transactions that have contributed to the Stripe
		 * account balance (includes charges, refunds, transfers, and so on).
		 * The transactions are returned in sorted order, with the most recent
		 * transactions appearing first.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends
				Method<BalanceTransactionCollection, AllMethod> {

			@Override
			public final BalanceTransactionCollection get()
					throws StripeException {
				return BalanceTransaction.all(params(), options());
			}

			/**
			 * A filter on the list based on the object
			 * <code>available_on</code> field. The value can be a string with
			 * an integer Unix timestamp, or it can be a dictionary with the
			 * following options:
			 */
			public final AllMethod availableOn(final String availableOn) {
				return set("available_on", availableOn);
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * 
			 */
			public final AllMethod currency(final String currency) {
				return set("currency", currency);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * Only returns transactions that are related to the specified
			 * Stripe object ID (e.g. filtering by a charge ID will return all
			 * charge and refund transactions).
			 */
			public final AllMethod source(final String source) {
				return set("source", source);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

			/**
			 * For automatic Stripe transfers only, only returns transactions
			 * that were transferred out on the specified transfer ID.
			 */
			public final AllMethod transfer(final String transfer) {
				return set("transfer", transfer);
			}

			/**
			 * Only returns transactions of the given type. One of:
			 * <code>charge</code>, <code>refund</code>, <code>adjustment</code>
			 * , <code>application_fee</code>,
			 * <code>application_fee_refund</code>, <code>transfer</code>, or
			 * <code>transfer_failure</code>
			 */
			public final AllMethod type(final String type) {
				return set("type", type);
			}

		} // AllMethod

	} // BalanceApi

	private final EventApi eventApi = new EventApi();

	/**
	 * <h1>Events</h1>
	 * <p>
	 * Events are our way of letting you know about something interesting that
	 * has just happened in your account. When an interesting event occurs, we
	 * create a new event object. For example, when a charge succeeds we create
	 * a charge.succeeded event; or, when an invoice can't be paid we create an
	 * invoice.payment_failed event. Note that many API requests may cause
	 * multiple events to be created. For example, if you create a new
	 * subscription for a customer, you will receive both a
	 * customer.subscription.created event and a charge.succeeded event.
	 * </p>
	 * <p>
	 * Like our other API resources, you can retrieve an individual event or a
	 * list of events from the API. We also have a system for sending the events
	 * directly to your server, called <a
	 * href="http://en.wikipedia.org/wiki/Webhook">webhooks</a>. Webhooks are
	 * managed in your <a
	 * href="https://dashboard.stripe.com/account/webhooks">account
	 * settings</a>, and our <a href="https://stripe.com/docs/webhooks">webhook
	 * guide</a> will help you get them set up.
	 * </p>
	 * <p>
	 * <strong>NOTE:</strong> Right now, we only guarantee access to events
	 * through the <a href="#retrieve_event">Retrieve Event API</a> for 30 days.
	 * </p>
	 */
	public final EventApi event() {
		return eventApi;
	}

	public final class EventApi {
		/**
		 * <h1>Retrieve an event</h1>
		 * <p>
		 * Retrieves the details of an event. Supply the unique identifier of
		 * the event, which you might have received in a webhook.
		 * </p>
		 * 
		 * @param id
		 *            The identifier of the event to be retrieved.
		 * 
		 */
		public final RetrieveMethod retrieve(final String id) {
			return new RetrieveMethod().set("id", id);
		}

		public final class RetrieveMethod extends Method<Event, RetrieveMethod> {

			@Override
			public final Event get() throws StripeException {
				return Event.retrieve((String) params().get("id"), options());
			}

		} // RetrieveMethod

		/**
		 * <h1>List all events</h1>
		 * <p>
		 * List events, going back up to 30 days.
		 * </p>
		 * 
		 */
		public final AllMethod all() {
			return new AllMethod();
		}

		public final class AllMethod extends Method<EventCollection, AllMethod> {

			@Override
			public final EventCollection get() throws StripeException {
				return Event.all(params(), options());
			}

			/**
			 * A filter on the list based on the object <code>created</code>
			 * field. The value can be a string with an integer Unix timestamp,
			 * or it can be a dictionary with the following options:
			 */
			public final AllMethod created(final String created) {
				return set("created", created);
			}

			/**
			 * A cursor for use in pagination. <code>ending_before</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, starting with
			 * <code>obj_bar</code>, your subsequent call can include
			 * <code>ending_before=obj_bar</code> in order to fetch the previous
			 * page of the list.
			 */
			public final AllMethod endingBefore(final String endingBefore) {
				return set("ending_before", endingBefore);
			}

			/**
			 * A limit on the number of objects to be returned. Limit can range
			 * between 1 and 100 items.
			 */
			public final AllMethod limit(final String limit) {
				return set("limit", limit);
			}

			/**
			 * A cursor for use in pagination. <code>starting_after</code> is an
			 * object ID that defines your place in the list. For instance, if
			 * you make a list request and receive 100 objects, ending with
			 * <code>obj_foo</code>, your subsequent call can include
			 * <code>starting_after=obj_foo</code> in order to fetch the next
			 * page of the list.
			 */
			public final AllMethod startingAfter(final String startingAfter) {
				return set("starting_after", startingAfter);
			}

			/**
			 * A string containing a specific event name, or group of events
			 * using * as a wildcard. The list will be filtered to include only
			 * events with a matching event property
			 */
			public final AllMethod type(final String type) {
				return set("type", type);
			}

		} // AllMethod

	} // EventApi
}
