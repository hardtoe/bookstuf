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
	// enable and fill in text fields, select
	form.find('input[type=text], textarea, select').each(function(index) {
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


function clearForm(form) {
	// clear text fields
	form.find('input[type=text], textarea').each(function(index) {
		var input = $(this);
		input.val('');
		input.parent().removeClass("is-dirty");
	});
	
	// reset radio buttons to default value
	form.find('label.mdl-radio.default-radio-button').each(function(index) {
		var label = $(this);
		var input = label.find('input[type=radio]');
		label.click();
	});
	
	// reset select fields to default value
	form.find('select').each(function(index) {
		var select = $(this);
		select.val('');
		select.removeClass('valid');
	});
}

function saveForm(form) {
	disableForm(form);
	
	// set saving indication
	form.find('.form-status').each(function(index) {
		var div = $(this);
		div.removeClass('form-idle form-saved form-save-fail').addClass('form-saving');
	});
	
	var data = getFormData(form, true);
	
	// post data
	$.ajax({
		type: "POST",
		url: form.attr("data-destination"),
		data: JSON.stringify(data),
		dataType: "json"			
	}).done(function(rsp) {
		if (hasUrlParameter("destination")) {
			// go back to account setup checklist
			window.location.href = getUrlParameter("destination");
			
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
}

function registerFormArrayEntryHandlers(parentForm, entry) {
	entry.find('.add-button').first().remove();
	entry.removeClass('sub-form-array-template').addClass('sub-form-array-entry');
	
	entry.find('.delete-button').click(function(index) {
		entry.remove();
		saveForm(parentForm);
	});
	
	entry.find('.save-button').click(function() {
		saveForm(parentForm);
	});
	
	initializeSelect(entry);
}


function setFormData(form, json, topLevel) {
	// fill in text fields
	var textInput = form.find("input[type=text], textarea, select");
	
	if (topLevel) {
		textInput = textInput.not(".sub-form *, .sub-form-array-template *");
	}
	
	textInput.each(function(index) {		
		var input = $(this);
		var id = input.attr("id").replace(/:.*/g, "");
		var value = json[id];
		
		if (id in json && !(value === "")) {
			input.val(value);
			input.addClass('valid');
			input.parent().addClass("is-dirty");
		}
	});

	// set radio buttons
	var radioInput = form.find("label.mdl-radio.is-checked");
	
	if (topLevel) {
		radioInput = radioInput.not(".sub-form *, .sub-form-array-template *");
	}
	
	radioInput.each(function(index) {
		var label = $(this);
		var input = label.find('input[type=radio]');
		var name = input.attr("name").replace(/:.*/g, "");	
		var value = json[name];
		
		if (name in json && !(value === "")) {	
			if (value === input.attr("value")) {
				label.click();
			}
		}
	});
	
	// set sub forms
	form.find('.sub-form-array').not(".sub-form *").each(function(index) {
		var subForm = $(this);
		
		if (subForm.attr('id') in json) {
			var dataArray = json[subForm.attr('id')];
			var template = subForm.find('.sub-form-array-template').first();
			
			jQuery.each(dataArray, function(index, item) {
				var entry = cloneFormFromTemplate(index, template, false);

				registerFormArrayEntryHandlers(form, entry);
				
				subForm.append(entry);	
				setFormData(entry, item, false);
			});
		}
	});
}

function getFormData(form, topLevel) {
	// gather form data
	var data = {};
	
	var textInput = form.find("input[type=text], textarea, select");
	
	if (topLevel) {
		textInput = textInput.not(".sub-form *, .sub-form-array-template *");
	}
	
	textInput.each(function(index) {
		var input = $(this);	
		var id = input.attr("id").replace(/:.*/g, "");
		data[id] = input.val();
	});
	
	var radioInput = form.find("label.mdl-radio.is-checked");
	
	if (topLevel) {
		radioInput = radioInput.not(".sub-form *, .sub-form-array-template *");
	}
	
	radioInput.each(function(index) {
		var label = $(this);	
		var input = label.find('input[type=radio]');
		var name = input.attr("name").replace(/:.*/g, "");
		data[name] = input.val();
	});
	
	// get sub forms
	form.find('.sub-form-array').each(function(index) {
		var subFormArray = $(this);
		var dataArray = [];
	
		subFormArray.find('.sub-form-array-entry').each(function(index) {
			var entry = $(this);
			dataArray.push(getFormData(entry, false));
		});
		
		data[subFormArray.attr('id')] = dataArray;
	});
	
	return data;
}

function renameIds(postfix, root) {
	root.find('[id]').each(function(index){
		var child = $(this);		
		child.attr('id', child.attr('id') + ':' + postfix);
	});
	
	root.find('[for]').each(function(index){
		var child = $(this);		
		child.attr('for', child.attr('for') + ':' + postfix);
	});
	
	root.find('*').each(function(index){
		var child = $(this);
		renameIds(postfix, child);
	});
}

/**
 * Performs a deep clone of a template.  Fills in input fields of the clone 
 * with the correct data.  Clears input fields of the template.
 * 
 * @param template Template element to clone from.
 */
function cloneFormFromTemplate(index, template, moveData) {
	var clone = template.clone();
	
	clone.find('.is-upgraded, [data-upgraded]').each(function(index) {
		var element = $(this);
		element.removeClass('is-upgraded');
		element.removeAttr('data-upgraded');
	});
	
	clone.find('button .mdl-button__ripple-container').remove();
	
	if (moveData) {
		setFormData(clone, getFormData(template, false), false);	
		clearForm(template);
	} else {
		clearForm(clone);
	}

	renameIds(index, clone);
	
	return clone;
}

// form data load and save
$(function() {
	$("div.form").each(function(index) {
		var form = $(this);
		
		// initialize select inputs
		initializeSelect(form);
		
		// set loading indication
		form.find('.form-status').each(function(index) {
			var div = $(this);
			div.addClass('form-loading');
		});
		
		// load data
		$.get(
			form.attr("data-source")
		).done(function(data) {
			var json = JSON.parse(data);

			enableForm(form);
			setFormData(form, json, true);

			// clear loading indication
			form.find('.form-status').each(function(index) {
				var div = $(this);
				div.removeClass('form-loading').addClass('form-idle');
			});

			componentHandler.upgradeDom();
		});
		
		// save data
		form.find('.save-button').click(function() {
			saveForm(form);
		});
		
		// add entry
		form.find('.sub-form-array').each(function(index) {
			var subFormArray = $(this);

			subFormArray.find('.sub-form-array-template').each(function(index) {
				var template = $(this);
				
				template.find('.add-button').click(function() {
					var index = subFormArray.children('.sub-form-array-entry').length;
					var entry = cloneFormFromTemplate(index, template, true);
					
					registerFormArrayEntryHandlers(form, entry);
					
					subFormArray.append(entry);
					componentHandler.upgradeDom();
					
					saveForm(form);
				});
			});
		});
	});
});

// Material Design Select
function initializeSelect(form) {
	form.find('select').each(function(index){
		$(this).on('change', function(ev) {
			var newClass = $(this).children(':selected').attr('disabled') ? 'invalid' : 'valid';
			$(this).attr('class', '').addClass(newClass);
		});
	})
}



///////////////////////////////////////////////////////////////////////////////////////////////////////
// {{{ CALENDAR PROTOTYPE
///////////////////////////////////////////////////////////////////////////////////////////////////////
function Calendar() {
	var self = this;
	
	// mustache template for calendar data
	self.calendarTemplate = 
		Hogan.compile($('#calendar-template').html());
	
	// height of service (50 pixels == 60 minutes)
	self.height = 50;
	
	self.yStart = 0;
	self.yEnd = self.yStart + self.height; 

	self.inFixedState = false;
	
	self.currentHover;
	self.currentAgenda;
	self.currentAgendaData;
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	self.tryOffset = function(pixel) {
		var yStartNew = (pixel + self.yStart);
		var yEndNew = (pixel + self.yEnd);
		
		if (
			yStartNew < 0 ||
			yEndNew > (24 * 50)
		) {
			// booking is off the agenda, invalid
			return false;
		}
		
		var hasSolution = true;
		
		for (i = 0; i < self.currentAgendaData.length; i++) {
			var booking =
				self.currentAgendaData[i];
			
			var bookingStart =
				booking['top'];
			
			var bookingEnd =
				booking['top'] + booking['height'];
			
			if (
				yStartNew >= bookingEnd ||
				bookingStart >= yEndNew
			) {
				// this one is good
				
			} else {
				hasSolution = false;
			}
		}

		if (hasSolution) {
			// here we have a good solution
			self.yStart = yStartNew;
			self.yEnd = yEndNew;
			self.currentHover.css('top', self.yStart + 'px');
			return true;
			
		} else {
			return false;
		}
	};

	///////////////////////////////////////////////////////////////////////////////////////////////////////
	self.updateTime = function() {
		var hour = Math.floor(self.yStart / 50);
		var minute = ((self.yStart % 50) * 60) / 50;
		var ampm;
		
		if (hour == 0) {
			hour = 12;
			ampm = 'AM';
			
		} else if (hour <= 11) {
			ampm = 'AM';
			
		} else if (hour == 12) {
			ampm = 'PM';
			
		} else {
			hour = hour - 12;
			ampm = 'PM';
		}
		
		$('#booking-time').text(hour + ':' + (minute < 10 ? '0' : '') + minute + ' ' + ampm);
	};
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	self.findClosestSolution = function() {
		// iterate over all 25 pixel increments (30 minutes)
		for (pixel = 0; pixel < (50 * 24); pixel += 25) {
			if (self.tryOffset(pixel)) {
				return true;
			}
			
			if (self.tryOffset(-pixel)) {
				return true;
			}
		}
		
		return false;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	self.setData = function(calendarData) {
		self.inFixedState = false;
		
		$('#calendar').html(self.calendarTemplate.render({'days': calendarData}));

		$(".day-agenda").mouseenter(function(event) {
			if (!self.inFixedState) {
				self.currentHover =
					$('<div class="booking-selection-hover" id="currentHover"></div>');

				self.currentHover.css('height', self.height + 'px');
				
				self.currentHover.click(function(event) {
					self.inFixedState = !self.inFixedState;
					
					if (self.inFixedState) {
						self.currentHover.addClass("booking-selection").removeClass("booking-selection-hover");
						
					} else {
						self.currentHover.addClass("booking-selection-hover").removeClass("booking-selection");
					}
				});
				
				self.currentAgenda =
					$(this)[0];
				
				var index = 
					$(this).attr('data-index');
				
				self.currentAgendaData =
					calendarData[index]['bookedTimes'];
				
				$(this).append(self.currentHover);
		
				if (self.findClosestSolution()) {
					// valid solution found, set date
					self.updateTime();
					$('#booking-date').text(calendarData[index]['day']);
					self.currentHover.text($('#booking-time').text() + ' ' + $('#serviceId').find(":selected").attr('data-title'));
					
				} else {
					// no valid location found
					self.currentHover.remove();
				}
			
			}
		});
		
		$(".day-agenda").mouseleave(function(event) {
			if (!self.inFixedState) {
				self.currentHover.remove();
			}
		});
		
		$(".day-agenda").mousemove(function(event) {
			if (!self.inFixedState) {
				self.yStart = (Math.round((event.clientY - self.currentAgenda.getBoundingClientRect()['top']) / 25.0) * 25) - (self.height / 2.0);
				self.yEnd = self.yStart + self.height;
			
				var valid = self.findClosestSolution();
				
				if (valid) {
					self.updateTime();
					self.currentHover.text($('#booking-time').text() + ' ' + $('#serviceId').find(":selected").attr('data-title'));
				}
			}
		});
	};		
}
///////////////////////////////////////////////////////////////////////////////////////////////////////
// }}} CALENDAR PROTOTYPE
///////////////////////////////////////////////////////////////////////////////////////////////////////
