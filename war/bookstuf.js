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

function setFormEnabled(form, disabled) {
	// enable and fill in text fields
	form.find('input[type=text], textarea').each(function(index) {
		var input = $(this);
		input.prop("disabled", disabled);
	});

	// enable and set radio buttons
	form.find('label.mdl-radio').each(function(index) {
		var label = $(this);
		var input = label.find('input[type=radio]');
		input.prop("disabled", disabled);
	});
		
	// remove any disabled styling on the form elements
	form.find('div, label').each(function(index){
		if (disabled) {
			$(this).addClass('is-disabled');
		} else {
			$(this).removeClass('is-disabled');
		}
	});
	
	// enable buttons
	form.find('button').prop("disabled", disabled);
}

function enableForm(form) {
	setFormEnabled(form, false);
}

function disableForm(form) {
	setFormEnabled(form, true);
	
}

function getUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1));
    var sURLVariables = sPageURL.split('&');
    var i;

    for (i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
}

function hasUrlParameter(sParam) {
    var sPageURL = decodeURIComponent(window.location.search.substring(1));
    var sURLVariables = sPageURL.split('&');
    var i;

    for (i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
            return true;
        }
    }
    
    return false;
}

// form data load and save
$(function() {
	$("div.form").each(function(index) {
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

					enableForm(form);
					
					// fill in text fields
					form.find('input[type=text], textarea').each(function(index) {
						var input = $(this);
						var value = json[input.attr("id")];
						
						if (value) {
							input.val(value);
							input.parent().addClass("is-dirty");
						}
					});

					// set radio buttons
					form.find('label.mdl-radio').each(function(index) {
						var label = $(this);
						var input = label.find('input[type=radio]');
						var value = json[input.attr("name")];
						
						if (value) {
							if (value === input.attr("value")) {
								label.click();
							}
						}
					});
					
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
				disableForm(form);
				
				// set saving indication
				form.find('.form-status').each(function(index) {
					var div = $(this);
					div.removeClass('form-idle form-saved form-save-fail').addClass('form-saving');
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
					if (hasUrlParameter("checklist")) {
						// go back to account setup checklist
						window.location.href = "checklist.html";
						
					} else {
						// clear saving indication
						form.find('.form-status').each(function(index) {
							var div = $(this);
							div.removeClass('form-saving').addClass('form-saved');
						});
					}
					
					
				}).fail(function(rsp) {
					// clear saving indication
					form.find('.form-status').each(function(index) {
						var div = $(this);
						div.removeClass('form-saving').addClass('form-save-fail');
					});
					
				}).always(function(rsp) {
					enableForm(form);
				});
				
				return false;
			});
		}
	});
});