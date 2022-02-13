;try {
/* module-key = 'com.atlassian.confluence.plugins.editor-loader:background-loading-editor', location = 'jscripts/editor-loader.js' */
AJS.Confluence.EditorLoader=function(d){var b,c={_listening:!1,_queuedHandlers:[],_watchHandler:function(){Confluence.Editor.UI.toggleWatchPage(!1)},_unwatchHandler:function(){Confluence.Editor.UI.toggleWatchPage(!0)},_createQueueAdder:function(a){return function(){c._listening&&c._queuedHandlers.push(a)}},bind:function(){AJS.bind("watchpage.pageoperation",this._createQueueAdder(this._watchHandler));AJS.bind("unwatchpage.pageoperation",this._createQueueAdder(this._unwatchHandler))},setListening:function(a){this._listening=
a},applyHandlers:function(){for(var a=this._queuedHandlers.pop();a;)a(),a=this._queuedHandlers.pop()}};c.setListening(!0);c.bind();var h=function(){var a=d("#editor-preload-container");a.length||(a=d('<div id="editor-preload-container" style="display: none;"></div>'));return a},e;return{getPreloadContainer:h,getEditorPreloadMarkup:function(){if(e)return e;var a=Confluence.getContextPath()+"/plugins/editor-loader/editor.action";return e=d.get(a,{parentPageId:AJS.Meta.get("parent-page-id"),pageId:AJS.Meta.get("page-id"),
spaceKey:AJS.Meta.get("space-key"),atl_after_login_redirect:window.location.pathname,timeout:AJS.Confluence.EditorLoader.loadingTimeout})},resourcesLoaded:function(){return b&&b.isResolved()},loadingTimeout:12E3,isEditorActive:function(){var a=d("#editor-preload-container");return a.length&&a.is(":visible")},load:function(a,c){var f;b?(b.fail(function(){c?c.call(this,arguments):AJS.log("EditorLoader: loadGuard - previous load failed.")}),b.done(function(){a?b.done(function(){setTimeout(a,0)}):AJS.log("EditorLoader: loadGuard - editor is already loaded.")}),
f=!0):f=void 0;if(!f){b=new d.Deferred;a&&b.done(a);c&&b.fail(c);var e=h();d("body").append(e);var g=new d.Deferred;AJS.Meta.get("page-id")?this.getEditorPreloadMarkup().done(function(a,b,c){if(b==="success"||b==="notmodified"){e.append(a);a=AJS.renderTemplate("dynamic-editor-metadata");d("head").append(a);AJS.populateParameters();AJS.debug("EditorLoader: Finished loading the editor template.");g.resolve()}else g.reject("Error loading the Editor template: "+c.status+" - "+c.statusText)}):g.resolve();
f=WRM.require(["wrc!editor","wrc!macro-browser","wrc!fullpage-editor"]).fail(function(a){AJS.logError("Failed to load editor resources",a)});d.when(g,f).done(function(){AJS.debug("EditorLoader: Finished loading the editor.");b.resolve()}).fail(function(){b.reject(arguments)})}},getEditorForm:function(){return this.isEditorActive()?d(tinymce.activeEditor.getContainer()).closest("form"):null}}}(AJS.$);
} catch (err) {
    if (console && console.log && console.error) {
        console.log("Error running batched script.");
        console.error(err);
    }
}

;
;try {
/* module-key = 'com.atlassian.confluence.plugins.editor-loader:background-loading-editor', location = 'jscripts/block-and-buffer-keys.js' */
AJS.Confluence.BlockAndBuffer={_cancelKeyboardFunction:function(a){a.preventDefault();a.stopPropagation()},_surrogatePairFixedFromCharCode:function(a){return 65535<a?(a-=65536,String.fromCharCode(55296+(a>>10),56320+(a&1023))):String.fromCharCode(a)},_bufferTextFunction:function(a,c){AJS.Confluence.BlockAndBuffer._cancelKeyboardFunction(a);var b=a.which;b||(b=a.charCode?a.charCode:a.keyCode);13!==b&&48>b||c.push(b)},_unblock:function(a,c,b){a.unbind("keypress",b);a="";for(b=0;b<c.length;b++)a+=AJS.Confluence.BlockAndBuffer._surrogatePairFixedFromCharCode(c[b]);
return a},block:function(a){var c=[],b=function(a){AJS.Confluence.BlockAndBuffer._bufferTextFunction(a,c);a.preventDefault()};a.keypress(b);return function(){return AJS.Confluence.BlockAndBuffer._unblock(a,c,b)}}};
} catch (err) {
    if (console && console.log && console.error) {
        console.log("Error running batched script.");
        console.error(err);
    }
}

;
