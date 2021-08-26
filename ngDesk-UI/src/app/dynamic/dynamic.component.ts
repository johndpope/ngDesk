import {
	Compiler,
	Component,
	Injector,
	NgModule,
	NgModuleRef,
	ViewChild,
	ViewContainerRef,
	Input,
	CompilerFactory,
	ComponentRef,
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';

@Component({
	selector: 'app-dynamic',
	template: '<div #vc></div>',
	styleUrls: ['./dynamic.component.scss'],
})
export class DynamicComponent {
	@ViewChild('vc', { read: ViewContainerRef }) _container: ViewContainerRef;

	@Input() public context: any;
	@Input() public template: string;
	@Input('material-module') public moduleData: any;
	public cmpRef: ComponentRef<any>;
	private isViewInitialized: boolean = false;

	constructor(
		private _compiler: Compiler,
		private _injector: Injector,
		private _m: NgModuleRef<any>
	) {}

	ngAfterViewInit() {
		this.isViewInitialized = true;
		this.updateComponent();
	}

	updateComponent() {
		if (!this.isViewInitialized) {
			return;
		}
		if (this.cmpRef) {
			this.cmpRef.destroy();
		}

		const tmpCmp = Component({ template: this.template })(class {});
		const tmpModule = NgModule({
			declarations: [tmpCmp],
			imports: this.moduleData.imports,
		})(class {});

		this._compiler
			.compileModuleAndAllComponentsAsync(tmpModule)
			.then((factories) => {
				const f = factories.componentFactories[0];
				this.cmpRef = f.create(this._injector, [], null, this._m);
				this.cmpRef.instance.context = this.context;
				this._container.insert(this.cmpRef.hostView);
			});
	}

	ngOnChanges() {
		this.updateComponent();
	}
}
