window.google.identitytoolkit.setConfig(
	{
		widgetUrl:	"https://www.bookstuf.com/gitkit",
		signOutUrl:	"/logout",
	}
);

// login/logout with destination
$(document).ready(function() {
	window.login = function() {
		Cookies.set('destination', window.location.pathname); 
		window.google.identitytoolkit.signIn(null);
	};
	
	window.logout = function() {
		Cookies.set('destination', window.location.pathname); 
		window.google.identitytoolkit.signOut(null);
	};
});

// sidebar navigation
$(document).ready(function() {
	$('section[id$=section]').hide();
	
	if (window.location.hash === '') {
		$('#calendar-section').show();
	} else {
		$(window.location.hash).show();
	}
	
	$('.mdl-navigation__link').click(function(){
        $('section[id$=section]').hide();
        
        var url = this.href; 
        var idx = url.indexOf("#");
        var hash = idx != -1 ? url.substring(idx) : "";
        
        $(hash).show();
        return true;
    });
});

// form data load and save
$(function() {
	$("form").each(function(index) {
		var form = $(this);

		// set loading indication
		form.find('.form-status').each(function(index) {
			var div = $(this);
			div.addClass('form-loading');
		});
		
		// load data
		if (form.attr("data-source") != null) {
			$.get(form.attr("data-source"))
				.done(function(data) {
					var json = JSON.parse(data);
					
					// enable and fill in text fields
					form.find('input[type=text], textarea').each(function(index) {
						var input = $(this);
						input.prop("disabled", false);
						
						var value = json[input.attr("id")];
						
						if (value) {
							input.val(value);
							input.parent().addClass("is-dirty");
						}
					});

					// enable and set radio buttons
					form.find('label.mdl-radio').each(function(index) {
						var label = $(this);
						var input = label.find('input[type=radio]');
						input.prop("disabled", false);
						
						var value = json[input.attr("name")];
						
						if (value) {
							if (value === input.attr("value")) {
								label.click();
							}
						}
					});
						
					// enable buttons
					form.find('button').prop("disabled", false);
					
					// clear loading indication
					form.find('.form-status').each(function(index) {
						var div = $(this);
						div.removeClass('form-loading').addClass('form-idle');
					});
				});
		}
		
		// save data
		if (form.attr("data-destination") != null) {	
			form.find('.save-button').click(function() {
				// set saving indication
				form.find('.form-status').each(function(index) {
					var div = $(this);
					div.removeClass('form-idle form-saved').addClass('form-saving');
				});
				
				// gather form data
				var data = {};
				
				form.find("input[type=text], textarea").each(function(index) {
					var input = $(this);	
					data[input.attr("id")] = input.val();
				});
				
				form.find("label.mdl-radio.is-checked").each(function(index) {
					var label = $(this);	
					var input = label.find('input[type=radio]');
					data[input.attr("name")] = input.val();
				});
				
				// post data
				$.ajax({
					type: "POST",
					url: form.attr("data-destination"),
					data: JSON.stringify(data),
					dataType: "json"
				}).done(function(rsp) {
					// clear saving indication
					form.find('.form-status').each(function(index) {
						var div = $(this);
						div.removeClass('form-saving').addClass('form-saved');
					});
				});
				
				return false;
			});
		}
	});
});