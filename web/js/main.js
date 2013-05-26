// The root URL for the RESTful services
var rootURL = "rest/devices";

var currentdevice;

// Retrieve device list when application starts 
//findAll();
findIDs();

// Register listeners
//Volume
$('#slider1').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Volume" + slider_value)
    updateDevice(currentdevice.identifier, 'VOL', slider_value, 'ZONETYPE_ROOM',true);
});

// Volume subzone
$('#slider2').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Volume subzone " + slider_value)
    updateDevice(currentdevice.identifier, 'VOL', slider_value, 'ZONETYPE_SUBZONE',true);
});

// Balance
$('#slider3').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Balance " + slider_value)
    updateDevice(currentdevice.identifier, 'BAL', slider_value, 'ZONETYPE_ROOM',true);
});

// Bas
$('#slider4').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Bas " + slider_value)
    updateDevice(currentdevice.identifier, 'BAS', slider_value, 'ZONETYPE_ROOM',true);
});

// Treb
$('#slider5').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Treb " + slider_value)
    updateDevice(currentdevice.identifier, 'TRB', slider_value, 'ZONETYPE_ROOM',true);
});

//Volume
$('#slider_UNIDISKSC_VOLUME').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Volume" + slider_value)
    updateDevice(currentdevice.identifier, 'VOL', slider_value, '',true);
});

// Balance
$('#slider3UnidiskSC').live('slidestop', function () {
    var slider_value = $(this).val()
    console.log("slider Balance " + slider_value)
    updateDevice(currentdevice.identifier, 'BAL', slider_value, 'ZONETYPE_ROOM',true);
});


$('#toggleswitch1').live('slidestop', function () {
    var slider_value = $(this).val();
    var totransport = '';
    if (slider_value == 'on')
        totransport = 'ON';
    else
        totransport = 'OFF';
    console.log("slider Mute " + totransport);
    updateDevice(currentdevice.identifier, 'MUTE', totransport, ZONETYPE_ROOM,false);
});

// Replace broken images with generic device bottle
$("img").error(function () {
    console.log("Error in finding picture");
    $(this).attr("src", "images/generic.jpg");

});

function findIDs() {
    console.log('findIDs');
    $.ajax({
        type:'GET',
        url:rootURL,
        dataType:"json", // data type of response
        success:initialize
    });
}

function findAll() {
    console.log('findAll');
    $.ajax({
        type:'GET',
        url:rootURL,
        dataType:"json", // data type of response
        success:renderList
    });
}

function findByName(searchKey) {
    console.log('findByName: ' + searchKey);
    $.ajax({
        type:'GET',
        url:rootURL + '/search/' + searchKey,
        dataType:"json",
        success:renderList
    });
}

// 1. get data from backend
// 2. show correct panel in devicecommands space
// 3. fill data correctly
function findById1(id) {
    console.log('findById: ' + id);
    $.mobile.showPageLoadingMsg();
    $.ajax({
        type:'GET',
        url:rootURL + '/' + id,
        dataType:"json",
        success:function (data) {
            console.log('findById1 success: ' + data.name);
            currentdevice = data;
            populateDeviceCommandsView(currentdevice);
            $.mobile.hidePageLoadingMsg();
        }
    });
}


function findById(id,url,options) {
    console.log('findById: ' + id);
    $.ajax({
        type:'GET',
        url:rootURL + '/' + id,
        dataType:"json",
        success:function (data) {
            console.log('findById success: ' + data);
            currentdevice = data;
            renderDetails(currentdevice,url,options);
        }
    });
}


function updateDevice(deviceID, key, value, zone, iseql) {
    deviceID = currentdevice.identifier;
    console.log("updateDevice: " + deviceID + "key " + key);
    $.ajax({
        type:'PUT',
        contentType:'application/json',
        url:rootURL + '/' + deviceID,
        dataType:"json",
        data:JSON.stringify({
            "deviceIdentifier":deviceID == "" ? null : deviceID,
            "key":key,
            "value":value,
            "zone":zone,
            "iseql":iseql
        }),
        success:function (data, textStatus, jqXHR) {
            alert('device updated successfully');
        },
        error:function (jqXHR, textStatus, errorThrown) {
            alert('update device error: ' + textStatus);
        }
    });
}

function initialize(data)
{
    var $page = $( "#pageDeviceCommands" );
    var $header = $page.children( ":jqmData(role=header)" );

    $.mobile.showPageLoadingMsg();

    console.log("initialize: " + data);

    $( "#pageDeviceOverview_List" ).html($( "#DeviceOverviewTemplate" ).render( data ));

    $header.find( "h3" ).html( "Please choose a device" );
    $( "#list_devices" ).html($( "#DevicePanelTemplate" ).render( data ));

    $( "#list_devices" ).listview("refresh");
    $.mobile.hidePageLoadingMsg();
}

function renderList(data) {

    console.log("renderList:" + data);
    // JAX-RS serializes an empty list as null, and a 'collection of one' as an object (not an 'array of one')
    var list = data == null ? [] : (data instanceof Array ? data : [data]);


    $('#deviceList div').remove();

    $.each(list, function (index, holder)
    {
        $.each(holder, function (key, device)
        {
            // yes, that is not nice and will not work with more than one device, FIXIT
            currentdevice = device;

            $('#deviceList').append('<div data-role="collapsible"  data-theme="b" data-content-theme="d"><h4>' + device.identifier + '</h4>' +
                '<div class="ui-grid-b">' +
                '<div class="ui-block-a">ID</div>' +
                '<div class="ui-block-b" id="deviceID">' + device.identifier + '</div>' +
                '<div class="ui-block-a">Counter Power</div>' +
                '<div class="ui-block-b" id="devicePower">' + device.power + '</div>' +
                '<div class="ui-block-a">Counter Mains</div>' +
                '<div class="ui-block-b" id="deviceMain">' + device.mains + '</div>' +
                '<div class="ui-block-a">Version Hardware</div>' +
                '<div class="ui-block-b" id="deviceHW">' + device.versionHW + '</div>' +
                '<div class="ui-block-a">Version Software</div>' +
                '<div class="ui-block-b" id="deviceSW">' + device.versionSW + '</div>' +
                '<div class="ui-block-a">Mute</div>' +
                '<div class="ui-block-b" id="deviceMute">' + device.mute + '</div>' +
                '<div class="ui-block-a">Listen</div>' +
                '<div class="ui-block-b" id="deviceListen">' + device.listen + '</div>' +
                '<div class="ui-block-a">Bas</div>' +
                '<div class="ui-block-b" id="deviceBas">' + device.bas + '</div>' +
                '<div class="ui-block-a">Treb</div>' +
                '<div class="ui-block-b" id="deviceTreb">' + device.treb + '</div>' +
                '<div class="ui-block-a">Volume</div>' +
                '<div class="ui-block-b" id="deviceVol">' + device.volume + '</div>' +
                '<div class="ui-block-a">Balance</div>' +
                '<div class="ui-block-b" id="deviceBal">' + device.balance + '</div>' +
                '<div class="ui-block-a">DaisyChain</div>' +
                '<div class="ui-block-b" id="deviceDaisy">' + device.daisyChain + '</div>' +
                '<div class="ui-block-a">Origin</div>' +
                '<div class="ui-block-b" id="origin">' + device.origin + '</div>' +
                '</div>' +

                '</div>');

            $('div[data-role=collapsible]').collapsible({refresh:true});


            if (device.volume != null) {

                $('#slider1').val(device.volume).slider({refresh:true});

                console.log("Setting Volume to " + $('#slider1').val());
            }
            /*
            if (device.balance != null) {
                $('#slider2').val(device.balance).slider({refresh:true});
                console.log("Setting balance to" + $('#slider2').val());
            }
            */
            if (device.balance != null) {
                $('#slider3').val(device.balance).slider({refresh:true});
                console.log("Setting balance to" + $('#slider3').val());
            }

            if (device.bas != null) {
                $('#slider4').val(device.bas).slider({refresh:true});
                console.log("Setting bas to" + $('#slider4').val());
            }

            if (device.treb != null) {
                $('#slider5').val(device.treb).slider({refresh:true});
                console.log("Setting treb to" + $('#slider5').val());
            }

            if (device.listen == 0) {
                $('#radio1').attr('checked',true);
                //$("input[value=src0]").attr('checked', true).checkboxradio('refresh');
            }
            else if (device.listen == 1) {
                $('#radio2').attr('checked',true);
                //$("input[value=src1]").attr('checked', true).checkboxradio('refresh');
            }
            else if (device.listen == 2) {
                $('#radio3').attr('checked',true);
                //$("input[value=src2]").attr('checked', true).checkboxradio('refresh');
            }
            else if (device.listen == 3) {
                $('#radio4').attr('checked',true);
                //$("input[value=src3]").attr('checked', true).checkboxradio('refresh');
            }

            if (device.mute == 'OFF')
                $('#toggleswitch1').val('off').slider("refresh");
            else
                $('#toggleswitch1').val('on').slider("refresh");
        });
    });
}

function populateDeviceCommandsView(currentdevice)
{
    var $page = $( "#pageDeviceCommands" );
    var $header = $page.children( ":jqmData(role=header)" );
    var $content = $page.children( ":jqmData(role=content)" );

    console.log("populateDeviceCommandsView for device " + currentdevice.identifier );
    $header.find( "h3" ).html( "Commands for " + currentdevice.identifier );

    $( "#contentDeviceCommands" ).html(
        $( "#" + currentdevice.identifier  + "Template" ).render( currentdevice )
    );


    document.mySwipe = new Swipe(document.getElementById(currentdevice.identifier  + "TemplateSwipe"));

    //set radio buttons correctly, need to check how that works with jsrender
    $("#radio" + currentdevice.listen + currentdevice.identifier).attr('checked',true);

    $page.trigger('pagecreate');

    if (currentdevice.mute == 'OFF')
        $('#toggleswitch1' + currentdevice.identifier).val('off').slider("refresh");
    else if(currentdevice.mute == 'ON')
        $('#toggleswitch1' + currentdevice.identifier).val('on').slider("refresh");
    else
        console.log("no switches available");

}


    function renderDetails(data,url,options)
    {
        console.log("renderDetails ...." + data.identifier)

        // Get the page we are going to dump our content into.
        var $page = $( "#category-items" );

        // Get the header for the page.
        var $header = $page.children( ":jqmData(role=header)" );

        // Get the content area element for the page.
        var $content = $page.children( ":jqmData(role=content)" );


        $( "#1contentDeviceCommands" ).html(
            $( "#DeviceDetailsTemplate" ).render( data )
        );

        // Pages are lazily enhanced. We call page() on the page
        // element to make sure it is always enhanced before we
        // attempt to enhance the listview markup we just injected.
        // Subsequent calls to page() are ignored since a page/widget
        // can only be enhanced once.
        $page.page();

        // Enhance the listview we just injected.
      //  $content.find( ":jqmData(role=listview)" ).listview();

        // We don't want the data-url of the page we just modified
        // to be the url that shows up in the browser's location field,
        // so set the dataUrl option to the URL for the category
        // we just loaded.
        options.dataUrl = url;

        // Now call changePage() and tell it to switch to
        // the page we just modified.
        $.mobile.changePage( $page, options );
    }


