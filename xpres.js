// Recursive-descent parser for basic xpres syntax.

var fs = require('fs');
var process = require('process');

var createLexer = function(text) {
    var pos = 0;

    return {
        nextToken: function() {
            var m = /^[ \t\n\r]+/.exec(text.substring(pos));
            if (m) { pos += m[0].length; }

            if (pos === text.length) {
                return { what: 'EOF' }
            }
            if (text[pos] === ';') {
                pos += 1;
                return { what: ';', text: ';' };
            }
            if (text[pos] === '=') {
                pos += 1;
                return { what: '=', text: '=' };
            }
            if (text.substring(pos, pos+3) === 'var') {
                pos += 3;
                return { what: 'var', text: 'var' };
            }
            if (text.substring(pos, pos+5) === 'print') {
                pos += 5;
                return { what: 'print', text: 'print' };
            }

            var m = /^[a-z]+/.exec(text.substring(pos));
            if (m) {
                pos += m[0].length;
                return { what: 'ID', text: m[0] };
            } else 
            
            var m = /^[0-9]+/.exec(text.substring(pos));
            if (m) {
                pos += m[0].length;
                return { what: 'INT', text: m[0] };
            }

            throw "No matched token at: " + text.substring(pos);
        }
    }
}

var parse = function(lex) {
    var vars = {};
    var exprVal;

    var getVar = function(name) {
        var v = vars[name];
        if (v === undefined) { throw "Undefined: " + name; }
        return v;
    }

    var parseExpr = function(firstTok) {
        if (firstTok.what == 'ID') {
            exprVal = getVar(firstTok.text).val;
        } else if (firstTok.what == 'INT') {
            exprVal = parseInt(firstTok.text);
        } else {
            throw "Invalid expression: " + firstTok.text;
        }
    }
    
    var parseDecl = function(firstTok) {
        if (firstTok.what != 'var') { throw "'var' expected: " + firstTok.text; }
        var id = lex.nextToken();
        if (id.what != 'ID') { throw "Invalid identifier: " + id.text; }
        if (vars[id.text] !== undefined) { throw "Redefined: " + id.text; }
        vars[id.text] = { val: 0 };
    }

    var parseAssign = function(firstTok) {
        if (firstTok.what != 'ID') { throw "Identifier expected: " + firstTok.text; }
        var v = getVar(firstTok.text);
        var ass = lex.nextToken();
        if (ass.what != '=') { throw "Assignment operator expected: " + ass.text; }
        parseExpr(lex.nextToken());
        v.val = exprVal;
    }

    var parsePrint = function(firstTok) {
        if (firstTok.what != 'print') { throw "'print' expected: " + firstTok.text; }
        parseExpr(lex.nextToken());
        console.log(exprVal);
    }

    var parseStmt = function(firstTok) {
        if (firstTok.what == 'var') { parseDecl(firstTok); }
        else if (firstTok.what == 'ID') { parseAssign(firstTok); }
        else if (firstTok.what == 'print') { parsePrint(firstTok); }
        else { throw "Unexpected: " + firstTok.text; }
    }

    var parseCode = function(firstTok) {
        var tok;

        if (firstTok.what != 'EOF') {
            parseStmt(firstTok);
            tok = lex.nextToken();
            if (tok.what != ';') { throw "Semicolon expected: " + tok.text; }
            tok = lex.nextToken();
            parseCode(tok);
        }
    }
        
    parseCode(lex.nextToken());
}

var lexer = createLexer(fs.readFileSync(process.argv[2], 'ascii'));        
parse(lexer);
