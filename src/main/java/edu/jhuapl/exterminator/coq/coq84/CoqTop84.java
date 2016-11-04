/*
 * Copyright (c) 2016, Johns Hopkins University Applied Physics
 * Laboratory All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.jhuapl.exterminator.coq.coq84;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import edu.jhuapl.exterminator.coq.CoqTop;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84AboutCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84AddToLoadPathCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84EVarsCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84GoalCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84HintsCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84InterpCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84RewindCommand;
import edu.jhuapl.exterminator.coq.coq84.command.Coq84StatusCommand;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84AboutMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84AddToLoadPathMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84EVarsMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84GoalMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84HintsMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84InterpMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84RewindMessage;
import edu.jhuapl.exterminator.coq.coq84.message.Coq84StatusMessage;

public class CoqTop84 extends CoqTop {

	/*
let add ?(logger=default_logger) x = eval_call ~logger (Xmlprotocol.add x)
let edit_at i = eval_call (Xmlprotocol.edit_at i)
let query ?(logger=default_logger) x = eval_call ~logger (Xmlprotocol.query x)
let mkcases s = eval_call (Xmlprotocol.mkcases s)
let status ?logger force = eval_call ?logger (Xmlprotocol.status force)
let hints x = eval_call (Xmlprotocol.hints x)
let search flags = eval_call (Xmlprotocol.search flags)
let init x = eval_call (Xmlprotocol.init x)
let stop_worker x = eval_call (Xmlprotocol.stop_worker x)

close
break
reset
get_arguments
set_arguments
goals



val add        : ?logger:Ideutils.logger ->
                 Interface.add_sty        -> Interface.add_rty query
val edit_at    : Interface.edit_at_sty    -> Interface.edit_at_rty query
val query      : ?logger:Ideutils.logger ->
                 Interface.query_sty      -> Interface.query_rty query
val status     : ?logger:Ideutils.logger ->
                 Interface.status_sty     -> Interface.status_rty query
val goals      : ?logger:Ideutils.logger ->
                 Interface.goals_sty      -> Interface.goals_rty query
val evars      : Interface.evars_sty      -> Interface.evars_rty query
val hints      : Interface.hints_sty      -> Interface.hints_rty query
val mkcases    : Interface.mkcases_sty    -> Interface.mkcases_rty query
val search     : Interface.search_sty     -> Interface.search_rty query
val init       : Interface.init_sty       -> Interface.init_rty query

val stop_worker: Interface.stop_worker_sty-> Interface.stop_worker_rty query


add ((s,eid),(sid,verbose))


rewind #
getoptions
mkcases s

https://github.com/coq/coq/blob/V8.4pl4/toplevel/ide_intf.ml
https://github.com/coq/coq/blob/V8.4pl4/toplevel/interface.mli
	 */

    private int interpID;

    public CoqTop84() throws IOException {
        super(EXE, "-ideslave");//, "-I", "../");//, "-I", "/usr/lib/coq");
        this.interpID = 0;
    }

    @Override
    public Coq84AddToLoadPathMessage addToLoadPath(Path directory) {
        Coq84AddToLoadPathCommand command = Coq84AddToLoadPathCommand.instance(interpID, directory);
        interpID++;
        return new Coq84AddToLoadPathMessage(command, send(command));
    }

    @Override
    public Coq84AboutMessage about() {
        Coq84AboutCommand command = Coq84AboutCommand.instance();
        return new Coq84AboutMessage(command, send(command));
    }

    @Override
    public Coq84StatusMessage status() {
        Coq84StatusCommand command = Coq84StatusCommand.instance();
        return new Coq84StatusMessage(command, send(command));
    }

    @Override
    public Coq84GoalMessage goal() {
        Coq84GoalCommand command = Coq84GoalCommand.instance();
        return new Coq84GoalMessage(command, send(command));
    }

    @Override
    public Coq84EVarsMessage evars() {
        Coq84EVarsCommand command = Coq84EVarsCommand.instance();
        return new Coq84EVarsMessage(command, send(command));
    }

    @Override
    public Coq84HintsMessage hints() {
        Coq84HintsCommand command = Coq84HintsCommand.instance();
        return new Coq84HintsMessage(command, send(command));
    }

    @Override
    public Coq84RewindMessage rewind(int steps) {
        Coq84RewindCommand command = Coq84RewindCommand.instance(steps);
        return new Coq84RewindMessage(command, send(command));
    }

    @Override
    public Coq84InterpMessage interp(String code) {
        Coq84InterpCommand command = Coq84InterpCommand.instance(interpID, Objects.requireNonNull(code));
        interpID++;
        return new Coq84InterpMessage(command, send(command));
    }

    ///////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws IOException {
        CoqTop84 coq = new CoqTop84();

        System.out.println(coq.about());

//		System.out.println("Enter lines to send to coqtop.  An empty line will send the message and print the response.");
//		
//		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//		String line = null;
//		List<String> commands = new ArrayList<>();
//		while((line = in.readLine()) != null) {
//			if(line.isEmpty()) {
//				List<String> response = coq.send(commands);
//				System.out.println("=============================");
//				for(String r : response) {
//					System.out.println(r);
//				}
//				System.out.println("=============================");
//				commands.clear();
//			} else {
//				commands.add(line);
//			}
//		}
    }

}
